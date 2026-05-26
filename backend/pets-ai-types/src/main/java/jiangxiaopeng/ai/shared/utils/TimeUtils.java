package jiangxiaopeng.ai.shared.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 统一的时间处理工具类
 * 
 * @author jiangyangang
 */
public final class TimeUtils {

    private TimeUtils() {}

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    private static final List<DateTimeFormatter> DATETIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    );

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd")
    );

    /**
     * 解析日期时间字符串为 Instant
     * 支持格式：
     * - ISO 8601: 2026-04-20T14:30:00Z
     * - 带秒: 2026-04-20T14:30:00
     * - 不带秒: 2026-04-20T14:30
     * - 带空格: 2026-04-20 14:30:00, 2026-04-20 14:30
     *
     * @param dateTimeStr 日期时间字符串
     * @return Instant
     * @throws IllegalArgumentException 如果格式不正确
     */
    public static Instant parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            throw new IllegalArgumentException("时间不能为空");
        }

        String trimmed = dateTimeStr.trim();

        if (trimmed.endsWith("Z") || trimmed.contains("+") || trimmed.matches(".*[+-]\\d{2}:\\d{2}$")) {
            try {
                return Instant.parse(trimmed);
            } catch (DateTimeParseException ignored) {}
        }

        for (DateTimeFormatter formatter : DATETIME_FORMATTERS) {
            try {
                if (formatter == DateTimeFormatter.ISO_INSTANT) {
                    continue;
                }
                LocalDateTime ldt = LocalDateTime.parse(trimmed, formatter);
                return ldt.atZone(DEFAULT_ZONE).toInstant();
            } catch (DateTimeParseException ignored) {}
        }

        throw new IllegalArgumentException(
                "时间格式不正确，支持格式: yyyy-MM-ddTHH:mm:ss, yyyy-MM-ddTHH:mm, yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 解析日期字符串为 LocalDate
     * 支持格式：
     * - ISO: 2026-04-20
     * - 斜杠: 2026/04/20
     * - 无分隔: 20260420
     *
     * @param dateStr 日期字符串
     * @return LocalDate
     * @throws IllegalArgumentException 如果格式不正确
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            throw new IllegalArgumentException("日期不能为空");
        }

        String trimmed = dateStr.trim();

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {}
        }

        throw new IllegalArgumentException("日期格式不正确，支持格式: yyyy-MM-dd, yyyy/MM/dd, yyyyMMdd");
    }

    /**
     * 将 Instant 转换为指定时区的 LocalDate
     *
     * @param instant Instant
     * @return LocalDate
     */
    public static LocalDate toLocalDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(DEFAULT_ZONE).toLocalDate();
    }

    /**
     * 将 Instant 转换为指定时区的 LocalDateTime
     *
     * @param instant Instant
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(DEFAULT_ZONE).toLocalDateTime();
    }

    /**
     * 将 LocalDate 转换为当天开始时间的 Instant
     *
     * @param date LocalDate
     * @return Instant
     */
    public static Instant toStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(DEFAULT_ZONE).toInstant();
    }

    /**
     * 将 LocalDate 转换为当天结束时间的 Instant (23:59:59.999999999)
     *
     * @param date LocalDate
     * @return Instant
     */
    public static Instant toEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX).atZone(DEFAULT_ZONE).toInstant();
    }

    /**
     * 将 LocalDateTime 转换为 Instant
     *
     * @param dateTime LocalDateTime
     * @return Instant
     */
    public static Instant toInstant(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(DEFAULT_ZONE).toInstant();
    }

    /**
     * 格式化 Instant 为 ISO 日期时间字符串
     *
     * @param instant Instant
     * @return 格式化后的字符串 (yyyy-MM-ddTHH:mm:ss)
     */
    public static String formatDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return toLocalDateTime(instant).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    /**
     * 格式化 Instant 为日期字符串
     *
     * @param instant Instant
     * @return 格式化后的字符串 (yyyy-MM-dd)
     */
    public static String formatDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return toLocalDate(instant).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * 格式化 Instant 为时间字符串
     *
     * @param instant Instant
     * @return 格式化后的字符串 (HH:mm:ss)
     */
    public static String formatTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return toLocalDateTime(instant).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * 获取当前时间的 Instant
     *
     * @return Instant
     */
    public static Instant now() {
        return Instant.now();
    }

    /**
     * 获取今天的日期
     *
     * @return LocalDate
     */
    public static LocalDate today() {
        return LocalDate.now(DEFAULT_ZONE);
    }

    /**
     * 获取默认时区
     *
     * @return ZoneId
     */
    public static ZoneId getDefaultZone() {
        return DEFAULT_ZONE;
    }
}
