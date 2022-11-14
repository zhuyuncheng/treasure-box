package top.zhuyuncheng.box.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Builder;
import lombok.Data;

/**
 * stating methods:
 * now()
 * <p>
 * Ending methods:
 * {@link #toDate()}
 * {@link #toString()}
 * {@link #toLong()}
 */
public final class DateTimes implements Comparable<DateTimes> {
    public static final String ISO_LOCAL_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String ISO_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    public static final DateTimeFormatter LOCAT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(ISO_LOCAL_DATETIME_PATTERN);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATETIME_PATTERN);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private LocalDateTime localDateTime;
    private DateTimeFormatter formatter;
    private ZoneId zoneId;

    /**
     * 当前时间，默认时间格式yyyy-MM-dd HH:mm:s，默认ZoneId
     *
     * @return
     */
    public static DateTimes now() {
        return new DateTimes();
    }

    /**
     * 当前时间
     *
     * @param pattern 时间格式
     * @return
     */
    public static DateTimes now(String pattern) {
        return now(pattern, ZoneId.systemDefault());
    }

    /**
     * 当前时间
     *
     * @param pattern 时间格式
     * @param zoneId  ZoneId
     * @return
     */
    public static DateTimes now(String pattern, ZoneId zoneId) {
        return new DateTimes(LocalDateTime.now(), DateTimeFormatter.ofPattern(pattern), zoneId);
    }

    /**
     * 某一时间点
     *
     * @param dateTimeStr 时间字符串
     * @return
     */
    public static DateTimes from(String dateTimeStr) {
        return from(dateTimeStr, ISO_DATETIME_PATTERN);
    }

    /**
     * 某一时间点
     *
     * @param dateTimeStr 时间字符串
     * @param pattern     时间格式
     * @return
     */
    public static DateTimes from(String dateTimeStr, String pattern) {
        return from(dateTimeStr, pattern, ZoneId.systemDefault());
    }

    /**
     * 某一时间点
     *
     * @param dateTimeStr 时间字符串
     * @param pattern     时间格式
     * @param zoneId      ZoneId
     * @return
     */
    public static DateTimes from(String dateTimeStr, String pattern, ZoneId zoneId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            return from(dateFormat.parse(dateTimeStr), pattern, zoneId);
        } catch (ParseException e) {
            throw new RuntimeException("Date analysis failed");
        }
    }

    /**
     * 某一时间点
     *
     * @param date Date时间
     * @return
     */
    public static DateTimes from(Date date) {
        return from(date, ISO_DATETIME_PATTERN);
    }

    /**
     * 某一时间点
     *
     * @param date    Date时间
     * @param pattern 时间格式
     * @return
     */
    public static DateTimes from(Date date, String pattern) {
        return from(date, pattern, ZoneId.systemDefault());
    }

    /**
     * 某一时间点
     *
     * @param date    Date时间
     * @param pattern 时间格式
     * @param zoneId  ZoneId
     * @return
     */
    public static DateTimes from(Date date, String pattern, ZoneId zoneId) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), zoneId);
        return new DateTimes(dateTime, DateTimeFormatter.ofPattern(pattern), zoneId);
    }

    /**
     * 某一时间点
     *
     * @param date 时间戳
     * @return
     */
    public static DateTimes from(Long date) {
        return from(date, ISO_DATETIME_PATTERN);
    }

    /**
     * 某一时间点
     *
     * @param date    时间戳
     * @param pattern 时间格式
     * @return
     */
    public static DateTimes from(Long date, String pattern) {
        return from(date, pattern, ZoneId.systemDefault());
    }

    /**
     * 某一时间点
     *
     * @param date    时间戳
     * @param pattern 时间格式
     * @param zoneId  ZoneId
     * @return
     */
    public static DateTimes from(Long date, String pattern, ZoneId zoneId) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(new Date(date).toInstant(), zoneId);
        return new DateTimes(dateTime, DateTimeFormatter.ofPattern(pattern), zoneId);
    }

    /**
     * 某一时间点
     *
     * @param dateTime LocalDateTime时间
     * @return
     */
    public static DateTimes from(LocalDateTime dateTime) {
        return from(dateTime, ISO_DATETIME_PATTERN);
    }

    /**
     * 某一时间点
     *
     * @param dateTime LocalDateTime时间
     * @param pattern  时间格式
     * @return
     */
    public static DateTimes from(LocalDateTime dateTime, String pattern) {
        return from(dateTime, pattern, ZoneId.systemDefault());
    }

    /**
     * 某一时间点
     *
     * @param dateTime LocalDateTime时间
     * @param pattern  时间格式
     * @param zoneId   ZoneId
     * @return
     */
    public static DateTimes from(LocalDateTime dateTime, String pattern, ZoneId zoneId) {
        return new DateTimes(dateTime, DateTimeFormatter.ofPattern(pattern), zoneId);
    }

    /**
     * 时间区间内的时间
     *
     * @param start 开始时间
     * @param end   结束时间
     * @param unit  时间单位：年、月、日、时、分、秒
     * @return
     */
    public static List<DateTimes> between(DateTimes start, DateTimes end, ChronoUnit unit) {
        return between(start, end, 1, unit);
    }

    /**
     * 时间区间内的时间
     *
     * @param start 开始时间
     * @param end   结束时间
     * @param step  时间单位步长
     * @param unit  时间单位：年、月、日、时、分、秒
     * @return
     */
    public static List<DateTimes> between(DateTimes start, DateTimes end, long step, ChronoUnit unit) {
        long distance = unit.between(start.localDateTime, end.localDateTime);

        UnaryOperator<DateTimes> operator;
        switch (unit) {
            case SECONDS:
                operator = u -> new DateTimes(u.plusSeconds(step));
                break;
            case MINUTES:
                operator = u -> new DateTimes(u.plusMinutes(step));
                break;
            case HOURS:
                operator = u -> new DateTimes(u.plusHours(step));
                break;
            case DAYS:
                operator = u -> new DateTimes(u.plusDays(step));
                break;
            case MONTHS:
                operator = u -> new DateTimes(u.plusMonths(step));
                break;
            case YEARS:
                operator = u -> new DateTimes(u.plusYears(step));
                break;
            default:
                throw new RuntimeException("Unsupported ChronoUnit " + unit);
        }

        return Stream.iterate(start, operator)
                .limit(distance + step)
                .collect(Collectors.toList());
    }

    private DateTimes() {
        this(LocalDateTime.now());
    }

    private DateTimes(LocalDateTime localDateTime) {
        this(localDateTime, DATETIME_FORMATTER);
    }

    private DateTimes(LocalDateTime localDateTime, DateTimeFormatter formatter) {
        this(localDateTime, formatter, ZoneId.systemDefault());
    }

    private DateTimes(LocalDateTime localDateTime, DateTimeFormatter formatter, ZoneId zoneId) {
        this.localDateTime = localDateTime;
        this.formatter = formatter;
        this.zoneId = zoneId;
    }

    private DateTimes(DateTimes dateTimes) {
        this.localDateTime = dateTimes.localDateTime;
        this.formatter = dateTimes.formatter;
        this.zoneId = dateTimes.zoneId;
    }

    /**
     * 指定时间格式
     *
     * @param pattern 时间格式
     * @return
     */
    public DateTimes formatter(String pattern) {
        this.formatter = DateTimeFormatter.ofPattern(pattern);
        return from(this.localDateTime.format(this.formatter), pattern, zoneId);
    }

    /**
     * 指定ZoneId
     *
     * @param zoneId ZoneId
     * @return
     */
    public DateTimes zoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
        this.localDateTime = this.localDateTime.atZone(zoneId).toLocalDateTime();
        return this;
    }

    /**
     * 天最大值
     *
     * @return
     */
    public DateTimes dayMax() {
        this.localDateTime = localDateTime.with(LocalTime.MAX);
        return this;
    }

    /**
     * 天最小值
     *
     * @return
     */
    public DateTimes dayMin() {
        this.localDateTime = localDateTime.with(LocalTime.MIN);
        return this;
    }

    /**
     * 月初
     *
     * @return
     */
    public DateTimes firstDayOfMonth() {
        this.localDateTime = localDateTime.with(TemporalAdjusters.firstDayOfMonth());
        return this;
    }

    /**
     * 月末
     *
     * @return
     */
    public DateTimes lastDayOfMonth() {
        this.localDateTime = localDateTime.with(TemporalAdjusters.lastDayOfMonth());
        return this;
    }

    /**
     * 加减年份
     *
     * @param years 年份
     * @return
     */
    public DateTimes plusYears(long years) {
        this.localDateTime = localDateTime.plusYears(years);
        return this;
    }

    /**
     * 加减月份
     *
     * @param months 月份
     * @return
     */
    public DateTimes plusMonths(long months) {
        this.localDateTime = localDateTime.plusMonths(months);
        return this;
    }

    /**
     * 加减周
     *
     * @param weeks 周
     * @return
     */
    public DateTimes plusWeeks(long weeks) {
        this.localDateTime = localDateTime.plusWeeks(weeks);
        return this;
    }

    /**
     * 加减日
     *
     * @param days 日
     * @return
     */
    public DateTimes plusDays(long days) {
        this.localDateTime = localDateTime.plusDays(days);
        return this;
    }

    /**
     * 渐渐小时
     *
     * @param hours 小时
     * @return
     */
    public DateTimes plusHours(long hours) {
        this.localDateTime = localDateTime.plusHours(hours);
        return this;
    }

    /**
     * 加减分钟
     *
     * @param minutes 分钟
     * @return
     */
    public DateTimes plusMinutes(long minutes) {
        this.localDateTime = localDateTime.plusMinutes(minutes);
        return this;
    }

    /**
     * 加减秒
     *
     * @param seconds 秒
     * @return
     */
    public DateTimes plusSeconds(long seconds) {
        this.localDateTime = localDateTime.plusSeconds(seconds);

        return this;
    }

    /**
     * 转为LocalDateTime类型
     *
     * @return Date
     */
    public LocalDateTime toLocalDateTime() {
        return this.localDateTime;
    }

    /**
     * 转为LocalDate类型
     *
     * @return Date
     */
    public LocalDate toLocalDate() {
        return this.localDateTime.toLocalDate();
    }

    /**
     * 转为LocalTime类型
     *
     * @return Date
     */
    public LocalTime toLocalTime() {
        return this.localDateTime.toLocalTime();
    }

    /**
     * 转为Date类型
     *
     * @return Date
     */
    public Date toDate() {
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }

    /**
     * 根据当前formatter转为String类型
     *
     * @return format date String
     */
    @Override
    public String toString() {
        return localDateTime.format(formatter);
    }

    /**
     * 转为时间戳类型
     *
     * @return Long time stamp
     */
    public Long toLong() {
        return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

    /**
     * 日期比较
     * > 0 : 大于比较日期
     * < 0 : 小于比较日志
     * = 0 : 相等
     *
     * @param dateTimes IBGDateTimes类型的日期
     * @return
     */
    @Override
    public int compareTo(DateTimes dateTimes) {
        return this.localDateTime.compareTo(Objects.requireNonNull(dateTimes).localDateTime);
    }

    /**
     * 比较日期返回日期差
     * {@link Difference#getYears()}    年份差
     * {@link Difference#getMonths()}   月份差
     * {@link Difference#getDays()}     日差
     * {@link Difference#getHours()}    时差
     * {@link Difference#getMinutes()}  分差
     * {@link Difference#getSeconds()}  秒差
     * {@link Difference#getMillis()}   毫秒差
     * {@link Difference#getNanos()}    纳秒差
     *
     * @param dateTimes
     * @return
     */
    public Difference difference(DateTimes dateTimes) {
        Objects.requireNonNull(dateTimes);
        Period period = Period.between(this.localDateTime.toLocalDate(), dateTimes.localDateTime.toLocalDate());
        Duration duration = Duration.between(this.localDateTime, dateTimes.localDateTime);
        return Difference.builder()
                .years(period.getYears())
                .months(period.toTotalMonths())
                .days(duration.toDays())
                .hours(duration.toHours())
                .minutes(duration.toMinutes())
                .seconds(duration.toMillis() / 1000)
                .millis(duration.toMillis())
                .nanos(duration.toNanos())
                .build();
    }

    @Data
    @Builder
    public static class Difference {
        private int years;
        private long months;
        private long days;
        private long hours;
        private long minutes;
        private long seconds;
        private long millis;
        private long nanos;
    }
}