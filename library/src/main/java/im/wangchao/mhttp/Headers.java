package im.wangchao.mhttp;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * <p>Description  : Headers.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午3:18.</p>
 */
public final class Headers {
    private final String[] namesAndValues;

    private Headers(Builder builder) {
        this.namesAndValues = builder.namesAndValues.toArray(new String[builder.namesAndValues.size()]);
    }

    private Headers(String[] namesAndValues) {
        this.namesAndValues = namesAndValues;
    }

    public String[] namesAndValues(){
        return namesAndValues;
    }

    /**
     * 返回 {@code name} 对应的最后一个 value
     */
    public String get(String name) {
        return get(namesAndValues, name);
    }

    /**
     * 将 {@code name} 对应的最后一个 value 解析成 Date，如果
     * 解析失败那么返回 null
     */
    public Date getDate(String name) {
        String value = get(name);
        return value != null ? HttpDate.parse(value) : null;
    }

    /**
     * 返回 head 数量
     */
    public int size() {
        return namesAndValues.length / 2;
    }

    /**
     * 返回 {@code position} 对应的 name
     */
    public String name(int index) {
        int nameIndex = index * 2;
        if (nameIndex < 0 || nameIndex >= namesAndValues.length) {
            return null;
        }
        return namesAndValues[nameIndex];
    }

    /**
     * 返回 {@code index} 对应的 value
     */
    public String value(int index) {
        int valueIndex = index * 2 + 1;
        if (valueIndex < 0 || valueIndex >= namesAndValues.length) {
            return null;
        }
        return namesAndValues[valueIndex];
    }

    /**
     * 返回 names 对应的 {@link Set}
     */
    public Set<String> names() {
        TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            result.add(name(i));
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * 返回 {@code name} 对应的 value 列表
     */
    public List<String> values(String name) {
        List<String> result = null;
        for (int i = 0, size = size(); i < size; i++) {
            if (name.equalsIgnoreCase(name(i))) {
                if (result == null) result = new ArrayList<>(2);
                result.add(value(i));
            }
        }
        return result != null
                ? Collections.unmodifiableList(result)
                : Collections.<String>emptyList();
    }

    public Builder newBuilder() {
        Builder result = new Builder();
        Collections.addAll(result.namesAndValues, namesAndValues);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0, size = size(); i < size; i++) {
            result.append(name(i)).append(": ").append(value(i)).append("\n");
        }
        return result.toString();
    }

    public Map<String, List<String>> toMultimap() {
        Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
        for (int i = 0, size = size(); i < size; i++) {
            String name = name(i);
            List<String> values = result.get(name);
            if (values == null) {
                values = new ArrayList<>(2);
                result.put(name, values);
            }
            values.add(value(i));
        }
        return result;
    }

    private static String get(String[] namesAndValues, String name) {
        for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
            if (name.equalsIgnoreCase(namesAndValues[i])) {
                return namesAndValues[i + 1];
            }
        }
        return null;
    }

    /**
     * 使用 String[] 创建 Headers，String[] 长度必须为偶数
     */
    public static Headers of(String... namesAndValues) {
        if (namesAndValues == null || namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected alternating header names and values");
        }

        // Make a defensive copy and clean it up.
        namesAndValues = namesAndValues.clone();
        for (int i = 0; i < namesAndValues.length; i++) {
            if (namesAndValues[i] == null) throw new IllegalArgumentException("Headers cannot be null");
            namesAndValues[i] = namesAndValues[i].trim();
        }

        // Check for malformed headers.
        for (int i = 0; i < namesAndValues.length; i += 2) {
            String name = namesAndValues[i];
            String value = namesAndValues[i + 1];
            if (name.length() == 0 || name.indexOf('\0') != -1 || value.indexOf('\0') != -1) {
                throw new IllegalArgumentException("Unexpected header: " + name + ": " + value);
            }
        }

        return new Headers(namesAndValues);
    }

    /**
     * 使用 {@link Map} 创建 Headers
     */
    public static Headers of(Map<String, String> headers) {
        if (headers == null) {
            throw new IllegalArgumentException("Expected map with header names and values");
        }

        // Make a defensive copy and clean it up.
        String[] namesAndValues = new String[headers.size() * 2];
        int i = 0;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getKey() == null || header.getValue() == null) {
                throw new IllegalArgumentException("Headers cannot be null");
            }
            String name = header.getKey().trim();
            String value = header.getValue().trim();
            if (name.length() == 0 || name.indexOf('\0') != -1 || value.indexOf('\0') != -1) {
                throw new IllegalArgumentException("Unexpected header: " + name + ": " + value);
            }
            namesAndValues[i] = name;
            namesAndValues[i + 1] = value;
            i += 2;
        }

        return new Headers(namesAndValues);
    }

    public static final class Builder {
        private final List<String> namesAndValues = new ArrayList<>(20);

        /**
         * Add a header line without any validation. Only appropriate for headers from the remote peer
         * or cache.
         */
        Builder addLenient(String line) {
            int index = line.indexOf(":", 1);
            if (index != -1) {
                return addLenient(line.substring(0, index), line.substring(index + 1));
            } else if (line.startsWith(":")) {
                // Work around empty header names and header names that start with a
                // colon (created by old broken SPDY versions of the response cache).
                return addLenient("", line.substring(1)); // Empty header name.
            } else {
                return addLenient("", line); // No header name.
            }
        }

        /** Add an header line containing a field name, a literal colon, and a value. */
        public Builder add(String line) {
            int index = line.indexOf(":");
            if (index == -1) {
                throw new IllegalArgumentException("Unexpected header: " + line);
            }
            return add(line.substring(0, index).trim(), line.substring(index + 1));
        }

        /**
         * 追加 header
         */
        public Builder add(String name, String value) {
            if (name == null) throw new IllegalArgumentException("name == null");
            if (value == null) throw new IllegalArgumentException("value == null");
            if (name.length() == 0 || name.indexOf('\0') != -1 || value.indexOf('\0') != -1) {
                throw new IllegalArgumentException("Unexpected header: " + name + ": " + value);
            }
            return addLenient(name, value);
        }

        /**
         * Add a field with the specified value without any validation. Only
         * appropriate for headers from the remote peer or cache.
         */
        Builder addLenient(String name, String value) {
            namesAndValues.add(name);
            namesAndValues.add(value.trim());
            return this;
        }

        public Builder removeAll(String name) {
            for (int i = 0; i < namesAndValues.size(); i += 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    namesAndValues.remove(i); // name
                    namesAndValues.remove(i); // value
                    i -= 2;
                }
            }
            return this;
        }

        /**
         * 设置 Header 并清空之前所有的设置
         */
        public Builder set(String name, String value) {
            removeAll(name);
            add(name, value);
            return this;
        }

        /**
         * 根据 {@code name} 获取 value
         */
        public String get(String name) {
            for (int i = namesAndValues.size() - 2; i >= 0; i -= 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    return namesAndValues.get(i + 1);
                }
            }
            return null;
        }

        public Headers build() {
            return new Headers(this);
        }
    }

    public static final class HttpDate {

        private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

        /**
         * Most websites serve cookies in the blessed format. Eagerly create the parser to ensure such
         * cookies are on the fast path.
         */
        private static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT =
                new ThreadLocal<DateFormat>() {
                    @Override
                    protected DateFormat initialValue() {
                        // RFC 2616 specified: RFC 822, updated by RFC 1123 format with fixed GMT.
                        DateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                        rfc1123.setLenient(false);
                        rfc1123.setTimeZone(GMT);
                        return rfc1123;
                    }
                };

        /** If we fail to parse a date in a non-standard format, try each of these formats in sequence. */
        private static final String[] BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS = new String[] {
                // HTTP formats required by RFC2616 but with any timezone.
                "EEE, dd MMM yyyy HH:mm:ss zzz", // RFC 822, updated by RFC 1123 with any TZ
                "EEEE, dd-MMM-yy HH:mm:ss zzz", // RFC 850, obsoleted by RFC 1036 with any TZ.
                "EEE MMM d HH:mm:ss yyyy", // ANSI C's asctime() format
                // Alternative formats.
                "EEE, dd-MMM-yyyy HH:mm:ss z",
                "EEE, dd-MMM-yyyy HH-mm-ss z",
                "EEE, dd MMM yy HH:mm:ss z",
                "EEE dd-MMM-yyyy HH:mm:ss z",
                "EEE dd MMM yyyy HH:mm:ss z",
                "EEE dd-MMM-yyyy HH-mm-ss z",
                "EEE dd-MMM-yy HH:mm:ss z",
                "EEE dd MMM yy HH:mm:ss z",
                "EEE,dd-MMM-yy HH:mm:ss z",
                "EEE,dd-MMM-yyyy HH:mm:ss z",
                "EEE, dd-MM-yyyy HH:mm:ss z",

      /* RI bug 6641315 claims a cookie of this format was once served by www.yahoo.com */
                "EEE MMM d yyyy HH:mm:ss z",
        };

        private static final DateFormat[] BROWSER_COMPATIBLE_DATE_FORMATS =
                new DateFormat[BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length];

        /** Returns the date for {@code value}. Returns null if the value couldn't be parsed. */
        public static Date parse(String value) {
            if (value.length() == 0) {
                return null;
            }

            ParsePosition position = new ParsePosition(0);
            Date result = STANDARD_DATE_FORMAT.get().parse(value, position);
            if (position.getIndex() == value.length()) {
                // STANDARD_DATE_FORMAT must match exactly; all text must be consumed, e.g. no ignored
                // non-standard trailing "+01:00". Those cases are covered below.
                return result;
            }
            synchronized (BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS) {
                for (int i = 0, count = BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length; i < count; i++) {
                    DateFormat format = BROWSER_COMPATIBLE_DATE_FORMATS[i];
                    if (format == null) {
                        format = new SimpleDateFormat(BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS[i], Locale.US);
                        // Set the timezone to use when interpreting formats that don't have a timezone. GMT is
                        // specified by RFC 2616.
                        format.setTimeZone(GMT);
                        BROWSER_COMPATIBLE_DATE_FORMATS[i] = format;
                    }
                    position.setIndex(0);
                    result = format.parse(value, position);
                    if (position.getIndex() != 0) {
                        // Something was parsed. It's possible the entire string was not consumed but we ignore
                        // that. If any of the BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS ended in "'GMT'" we'd have
                        // to also check that position.getIndex() == value.length() otherwise parsing might have
                        // terminated early, ignoring things like "+01:00". Leaving this as != 0 means that any
                        // trailing junk is ignored.
                        return result;
                    }
                }
            }
            return null;
        }

        /** Returns the string for {@code value}. */
        public static String format(Date value) {
            return STANDARD_DATE_FORMAT.get().format(value);
        }

        private HttpDate() {
        }
    }
}
