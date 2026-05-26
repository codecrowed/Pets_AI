package jiangxiaopeng.ai.shared.infrastructure.persistence;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Automatically appends logical-delete predicate for tables that own delete_flag.
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class MybatisLogicalDeleteInterceptor implements Interceptor {

    private static final Pattern FROM_OR_JOIN_PATTERN =
            Pattern.compile("(?i)\\b(from|join)\\s+([a-zA-Z0-9_\\.\\\"]+)(?:\\s+(?:as\\s+)?([a-zA-Z0-9_\\\"]+))?");

    private static final Pattern TAIL_CLAUSE_PATTERN =
            Pattern.compile("(?i)\\b(group\\s+by|order\\s+by|limit|offset|fetch|for\\s+update)\\b");

    private static final Set<String> TABLE_HAS_DELETE_FLAG_CACHE = ConcurrentHashMap.newKeySet();
    private static final Set<String> TABLE_NO_DELETE_FLAG_CACHE = ConcurrentHashMap.newKeySet();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();
        if (originalSql == null) {
            return invocation.proceed();
        }

        String trimmedSql = originalSql.trim();
        if (!trimmedSql.regionMatches(true, 0, "select", 0, "select".length())) {
            return invocation.proceed();
        }
        if (trimmedSql.toLowerCase(Locale.ROOT).contains("delete_flag")) {
            return invocation.proceed();
        }

        Connection connection = (Connection) invocation.getArgs()[0];
        List<TableRef> tableRefs = extractTableRefs(trimmedSql);
        if (tableRefs.isEmpty()) {
            return invocation.proceed();
        }

        List<String> predicates = new ArrayList<>();
        for (TableRef tableRef : tableRefs) {
            String tableName = normalizeTableName(tableRef.tableName());
            if (tableName.isBlank()) {
                continue;
            }
            if (hasDeleteFlagColumn(connection, tableName)) {
                String qualifier = tableRef.alias() != null && !tableRef.alias().isBlank()
                        ? stripQuotes(tableRef.alias())
                        : tableName;
                predicates.add(qualifier + ".delete_flag = 0");
            }
        }
        if (predicates.isEmpty()) {
            return invocation.proceed();
        }

        String enhancedSql = appendPredicate(trimmedSql, String.join(" AND ", predicates));
        setBoundSql(boundSql, enhancedSql);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    private static List<TableRef> extractTableRefs(String sql) {
        Matcher matcher = FROM_OR_JOIN_PATTERN.matcher(sql);
        List<TableRef> refs = new ArrayList<>();
        while (matcher.find()) {
            String rawTable = stripQuotes(matcher.group(2));
            String alias = stripQuotes(matcher.group(3));
            refs.add(new TableRef(rawTable, alias));
        }
        return refs;
    }

    private static String appendPredicate(String sql, String predicate) {
        Matcher matcher = TAIL_CLAUSE_PATTERN.matcher(sql);
        int tailStart = matcher.find() ? matcher.start() : sql.length();
        String beforeTail = sql.substring(0, tailStart).trim();
        String tail = sql.substring(tailStart);

        if (beforeTail.toLowerCase(Locale.ROOT).contains(" where ")) {
            return beforeTail + " AND " + predicate + " " + tail;
        }
        return beforeTail + " WHERE " + predicate + " " + tail;
    }

    private static String normalizeTableName(String tableName) {
        String normalized = stripQuotes(tableName);
        int dot = normalized.indexOf('.');
        if (dot >= 0) {
            normalized = normalized.substring(dot + 1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static String stripQuotes(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        if (result.startsWith("\"") && result.endsWith("\"") && result.length() > 1) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    private static boolean hasDeleteFlagColumn(Connection connection, String tableName) throws SQLException {
        if (TABLE_HAS_DELETE_FLAG_CACHE.contains(tableName)) {
            return true;
        }
        if (TABLE_NO_DELETE_FLAG_CACHE.contains(tableName)) {
            return false;
        }

        boolean hasColumn = false;
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, "delete_flag")) {
            hasColumn = rs.next();
        }

        if (hasColumn) {
            TABLE_HAS_DELETE_FLAG_CACHE.add(tableName);
        } else {
            TABLE_NO_DELETE_FLAG_CACHE.add(tableName);
        }
        return hasColumn;
    }

    private static void setBoundSql(BoundSql boundSql, String sql) {
        try {
            Field sqlField = BoundSql.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            sqlField.set(boundSql, sql);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to rewrite SQL in MyBatis interceptor", e);
        }
    }

    private record TableRef(String tableName, String alias) {
    }
}
