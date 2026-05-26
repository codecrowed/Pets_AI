package jiangxiaopeng.ai.ai.infrastructure.agent;

import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiToolConfigEntity;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 解析 {@code pet_ai_tool_config}：
 * <ul>
 *   <li>{@code method}：本地 Spring Bean + {@code @Tool}，按 {@code tool_name} 匹配</li>
 *   <li>{@code mcp}：从容器中注册的 {@link ToolCallbackProvider} 收集 MCP {@link ToolCallback}</li>
 * </ul>
 * MCP 的 {@code tool_name} 支持两种写法：
 * <ul>
 *   <li>{@code 仅工具名}：与 {@link ToolCallback#getToolDefinition()}{@code .name()} 相等即可（多 server 同名时取第一个匹配）</li>
 *   <li>{@code serverKey:toolName}：仅第一段 {@code serverKey} 后第一个 {@code :} 分割，
 *       serverKey 与 MCP 握手返回的 {@code serverInfo.name()} 忽略大小写相等，
 *       toolName 与工具定义名一致，用于消解不同 MCP server 下的同名工具</li>
 * </ul>
 */
@Component
public class PetAiToolCallbackResolver {

    private static final String TOOL_TYPE_METHOD = "method";
    private static final String TOOL_TYPE_MCP = "mcp";

    private static final String SYNC_MCP_CB = "org.springframework.ai.mcp.SyncMcpToolCallback";
    private static final String ASYNC_MCP_CB = "org.springframework.ai.mcp.AsyncMcpToolCallback";
    private static final String FIELD_SYNC_CLIENT = "mcpClient";
    private static final String FIELD_ASYNC_CLIENT = "asyncMcpClient";

    private final PetAiMethodToolRegistry methodToolRegistry;
    private final ObjectProvider<ToolCallbackProvider> toolCallbackProviders;

    public PetAiToolCallbackResolver(
            PetAiMethodToolRegistry methodToolRegistry,
            ObjectProvider<ToolCallbackProvider> toolCallbackProviders) {
        this.methodToolRegistry = methodToolRegistry;
        this.toolCallbackProviders = toolCallbackProviders;
    }

    public Optional<ToolCallback> resolve(PetAiToolConfigEntity toolConfig) {
        if (toolConfig == null || toolConfig.getToolName() == null || toolConfig.getToolName().isBlank()) {
            return Optional.empty();
        }
        String toolType = toolConfig.getToolType() == null ? "" : toolConfig.getToolType().trim().toLowerCase(Locale.ROOT);
        return switch (toolType) {
            case TOOL_TYPE_METHOD -> resolveMethod(toolConfig.getToolName());
            case TOOL_TYPE_MCP -> resolveMcp(toolConfig.getToolName());
            default -> Optional.empty();
        };
    }

    public Optional<ToolCallback> resolveMethod(String toolName) {
        return methodToolRegistry.resolveMethodToolBean(toolName)
                .flatMap(toolBean -> Arrays.stream(MethodToolCallbackProvider.builder()
                                .toolObjects(toolBean)
                                .build()
                                .getToolCallbacks())
                        .filter(cb -> toolName.equals(cb.getToolDefinition().name()))
                        .findFirst());
    }

    private Optional<ToolCallback> resolveMcp(String configuredToolName) {
        String trimmed = configuredToolName.trim();
        McpToolRef ref = McpToolRef.parse(trimmed);
        List<ToolCallback> all = toolCallbackProviders.orderedStream()
                .flatMap(provider -> Arrays.stream(provider.getToolCallbacks()))
                .toList();

        Stream<ToolCallback> stream = all.stream();
        if (ref.serverKey() != null) {
            stream = stream.filter(cb -> ref.serverKey().equalsIgnoreCase(extractMcpServerName(cb).orElse("")));
        }
        stream = stream.filter(cb -> toolDefinitionNameMatches(cb, ref.toolName()));

        return stream.findFirst();
    }

    private static boolean toolDefinitionNameMatches(ToolCallback cb, String expectedToolName) {
        String name = cb.getToolDefinition().name();
        return expectedToolName.equals(name) || expectedToolName.equalsIgnoreCase(name);
    }

    /** 从 Spring AI MCP 回调上解析 MCP server 名称（握手 {@code serverInfo.name}）。 */
    private static Optional<String> extractMcpServerName(ToolCallback cb) {
        if (cb == null) {
            return Optional.empty();
        }
        String cn = cb.getClass().getName();
        try {
            if (SYNC_MCP_CB.equals(cn)) {
                return serverNameFromMcpClient(readField(cb, FIELD_SYNC_CLIENT));
            }
            if (ASYNC_MCP_CB.equals(cn)) {
                return serverNameFromMcpClient(readField(cb, FIELD_ASYNC_CLIENT));
            }
        } catch (ReflectiveOperationException ignored) {
            // fall through
        }
        return Optional.empty();
    }

    private static Object readField(Object target, String fieldName) throws ReflectiveOperationException {
        Class<?> c = target.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static Optional<String> serverNameFromMcpClient(Object mcpClient) {
        if (mcpClient == null) {
            return Optional.empty();
        }
        try {
            Method getServerInfo = mcpClient.getClass().getMethod("getServerInfo");
            Object impl = getServerInfo.invoke(mcpClient);
            if (impl == null) {
                return Optional.empty();
            }
            Method name = impl.getClass().getMethod("name");
            Object n = name.invoke(impl);
            return n instanceof String s ? Optional.of(s) : Optional.empty();
        } catch (ReflectiveOperationException e) {
            return Optional.empty();
        }
    }

    private record McpToolRef(String serverKey, String toolName) {
        static McpToolRef parse(String configured) {
            int idx = configured.indexOf(':');
            if (idx <= 0 || idx >= configured.length() - 1) {
                return new McpToolRef(null, configured);
            }
            String server = configured.substring(0, idx).trim();
            String tool = configured.substring(idx + 1).trim();
            if (server.isEmpty() || tool.isEmpty()) {
                return new McpToolRef(null, configured);
            }
            return new McpToolRef(server, tool);
        }
    }
}
