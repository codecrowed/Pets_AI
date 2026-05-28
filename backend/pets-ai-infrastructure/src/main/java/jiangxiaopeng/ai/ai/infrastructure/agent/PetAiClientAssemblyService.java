package jiangxiaopeng.ai.ai.infrastructure.agent;

import jiangxiaopeng.ai.ai.infrastructure.agent.model.HttpClientConfigRecord;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiClientConfigEntity;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.fastjson.JSONObject;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 {@code pet_ai_client_config} 装配 {@link OpenAiApi}、{@link ChatModel}、无状态 {@link ChatClient}。
 */
@Service
public class PetAiClientAssemblyService {

    private final ConcurrentHashMap<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final double temperature;
    private final int maxTokens;

    public PetAiClientAssemblyService(
            @Value("${spring.ai.openai.chat.options.temperature}") double temperature,
            @Value("${spring.ai.openai.chat.options.max-tokens}") int maxTokens) {
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    public ChatModel getOrCreateChatModel(PetAiClientConfigEntity client, String modelName) {
        String key = client.getClientConfigId() + ":" + modelName;
        return chatModelCache.computeIfAbsent(key, k -> buildChatModel(client, modelName));
    }

    /**
     * 与 Spring Boot 配置一致，用于库表无数据时的回退。
     */
    public ChatModel buildChatModelFromYml(
            String baseUrl,
            String apiKey,
            String completionsPath,
            String embeddingsPath,
            String modelName) {
        OpenAiApi openAiApi = buildOpenAiApi(baseUrl, apiKey, completionsPath, embeddingsPath);
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(modelName)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .build())
                .build();
    }

    public ChatClient createStatelessChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    public ChatClient createStatelessChatClient(ChatModel chatModel, List<ToolCallback> toolCallbacks) {
        var builder = ChatClient.builder(chatModel);

        if (toolCallbacks != null && !toolCallbacks.isEmpty()) {
            builder.defaultToolCallbacks(toolCallbacks);
        }
        
        return builder.build();
    }

    private ChatModel buildChatModel(PetAiClientConfigEntity client, String modelName) {
        OpenAiApi openAiApi = buildOpenAiApi(
                client.getBaseUrl(),
                client.getApiKey(),
                client.getCompletionsPath(),
                client.getEmbeddingsPath(),
                client.getHttpClientConfig());
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(modelName)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .build())
                .build();
    }

    private static OpenAiApi buildOpenAiApi(String baseUrl, 
        String apiKey, 
        String completionsPath, 
        String embeddingsPath) {

        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .completionsPath(trimLeadingSlash(completionsPath))
                .embeddingsPath(trimLeadingSlash(embeddingsPath))
                .restClientBuilder(RestClient.builder()
                        .requestFactory(new JdkClientHttpRequestFactory()))
                .webClientBuilder(WebClient.builder()
                        .clientConnector(new JdkClientHttpConnector()))
                .build();
    }

    private static OpenAiApi buildOpenAiApi(
        String baseUrl, 
        String apiKey, 
        String completionsPath, 
        String embeddingsPath,
        String httpClientConfig) {

        HttpClientConfigRecord httpClientConfigRecord = JSONObject.parseObject(httpClientConfig, HttpClientConfigRecord.class);

        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .completionsPath(trimLeadingSlash(completionsPath))
                .embeddingsPath(trimLeadingSlash(embeddingsPath))
                .restClientBuilder(RestClient.builder()
                        .requestFactory(buildJdkClientHttpRequestFactory(httpClientConfigRecord)))
                .webClientBuilder(WebClient.builder()
                        .clientConnector(buildHttpConnector(httpClientConfigRecord)))
                .build();
    }

    /**
     * 构建 JdkClientHttpConnector
     * @param httpClientConfigRecord
     * @return
     */
    private static JdkClientHttpConnector buildHttpConnector(HttpClientConfigRecord httpClientConfigRecord) {
        
        JdkClientHttpConnector jdkClientHttpConnector = new JdkClientHttpConnector(
            HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(httpClientConfigRecord.connectTimeout()))
            .build()
        );

        jdkClientHttpConnector.setReadTimeout(Duration.ofSeconds(httpClientConfigRecord.readTimeout()));

        return jdkClientHttpConnector;
    }

    /**
     * 构建 JdkClientHttpRequestFactory
     * @param httpClientConfigRecord
     * @return
     */
    private static JdkClientHttpRequestFactory buildJdkClientHttpRequestFactory(HttpClientConfigRecord httpClientConfigRecord) {
        JdkClientHttpRequestFactory jdkClientHttpRequestFactory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(httpClientConfigRecord.connectTimeout()))
            .build()
        );
        
        jdkClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(httpClientConfigRecord.readTimeout()));

        return jdkClientHttpRequestFactory;
    }

    private static String trimLeadingSlash(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
