package com.skyeye.common.util;

import com.skyeye.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具类
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
public class DateUtils {

    /** 默认时区 */
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");
    
    /** 常用格式化器 */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.Time.DEFAULT_DATE_FORMAT);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(Constants.Time.DEFAULT_TIME_FORMAT);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(Constants.Time.DEFAULT_DATETIME_FORMAT);
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(Constants.Time.ISO_DATETIME_FORMAT);

    /**
     * 获取当前时间戳
     * 
     * @return 当前时间戳
     */
    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 获取当前LocalDateTime
     * 
     * @return 当前LocalDateTime
     */
    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now(DEFAULT_ZONE_ID);
    }

    /**
     * 获取当前LocalDate
     * 
     * @return 当前LocalDate
     */
    public static LocalDate nowLocalDate() {
        return LocalDate.now(DEFAULT_ZONE_ID);
    }

    /**
     * 获取当前LocalTime
     * 
     * @return 当前LocalTime
     */
    public static LocalTime nowLocalTime() {
        return LocalTime.now(DEFAULT_ZONE_ID);
    }

    /**
     * 格式化日期时间
     * 
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化日期时间（自定义格式）
     * 
     * @param dateTime 日期时间
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) {
            return null;
        }
        try {
            return dateTime.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.error("日期格式化失败: dateTime={}, pattern={}", dateTime, pattern, e);
            return null;
        }
    }

    /**
     * 格式化日期
     * 
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 格式化时间
     * 
     * @param time 时间
     * @return 格式化后的字符串
     */
    public static String format(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(TIME_FORMATTER);
    }

    /**
     * 格式化Timestamp
     * 
     * @param timestamp 时间戳
     * @return 格式化后的字符串
     */
    public static String format(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().format(DATETIME_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     * 
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            log.error("日期时间解析失败: {}", dateTimeStr, e);
            return null;
        }
    }

    /**
     * 解析日期时间字符串（自定义格式）
     * 
     * @param dateTimeStr 日期时间字符串
     * @param pattern 格式模式
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty() || pattern == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.error("日期时间解析失败: dateTimeStr={}, pattern={}", dateTimeStr, pattern, e);
            return null;
        }
    }

    /**
     * 解析日期字符串
     * 
     * @param dateStr 日期字符串
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.error("日期解析失败: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 解析时间字符串
     * 
     * @param timeStr 时间字符串
     * @return LocalTime
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (Exception e) {
            log.error("时间解析失败: {}", timeStr, e);
            return null;
        }
    }

    /**
     * LocalDateTime转Timestamp
     * 
     * @param dateTime LocalDateTime
     * @return Timestamp
     */
    public static Timestamp toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Timestamp.valueOf(dateTime);
    }

    /**
     * Timestamp转LocalDateTime
     * 
     * @param timestamp Timestamp
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    /**
     * Date转LocalDateTime
     * 
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDateTime();
    }

    /**
     * LocalDateTime转Date
     * 
     * @param dateTime LocalDateTime
     * @return Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(DEFAULT_ZONE_ID).toInstant());
    }

    /**
     * 获取日期的开始时间（00:00:00）
     * 
     * @param date 日期
     * @return 开始时间
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * 获取日期的结束时间（23:59:59.999）
     * 
     * @param date 日期
     * @return 结束时间
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

    /**
     * 计算两个日期之间的天数差
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 计算两个时间之间的小时差
     * 
     * @param startDateTime 开始时间
     * @param endDateTime 结束时间
     * @return 小时差
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * 计算两个时间之间的分钟差
     * 
     * @param startDateTime 开始时间
     * @param endDateTime 结束时间
     * @return 分钟差
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    /**
     * 计算两个时间之间的秒差
     * 
     * @param startDateTime 开始时间
     * @param endDateTime 结束时间
     * @return 秒差
     */
    public static long secondsBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(startDateTime, endDateTime);
    }

    /**
     * 判断日期是否在指定范围内
     * 
     * @param date 要判断的日期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return true如果在范围内
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * 判断时间是否在指定范围内
     * 
     * @param dateTime 要判断的时间
     * @param startDateTime 开始时间
     * @param endDateTime 结束时间
     * @return true如果在范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (dateTime == null || startDateTime == null || endDateTime == null) {
            return false;
        }
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }

    /**
     * 获取当前时间戳（毫秒）
     * 
     * @return 时间戳
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间戳（秒）
     * 
     * @return 时间戳
     */
    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 毫秒时间戳转LocalDateTime
     * 
     * @param timestamp 毫秒时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), DEFAULT_ZONE_ID);
    }

    /**
     * LocalDateTime转毫秒时间戳
     * 
     * @param dateTime LocalDateTime
     * @return 毫秒时间戳
     */
    public static long toTimestampMillis(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }
        return dateTime.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
    }
} 