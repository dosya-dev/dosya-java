package dev.dosya.sdk.internal;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Reads integers leniently: a decimal such as {@code 7.5} is truncated instead of failing
 * the whole response, numeric strings are accepted, and null stays null.
 */
final class LenientNumber {

    static final TypeAdapter<Integer> INTEGER = new TypeAdapter<Integer>() {
        @Override
        public void write(JsonWriter out, Integer value) throws IOException {
            if (value == null) out.nullValue();
            else out.value(value);
        }

        @Override
        public Integer read(JsonReader in) throws IOException {
            Long v = readLong(in);
            if (v == null) return null;
            return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, v));
        }
    };

    static final TypeAdapter<Long> LONG = new TypeAdapter<Long>() {
        @Override
        public void write(JsonWriter out, Long value) throws IOException {
            if (value == null) out.nullValue();
            else out.value(value);
        }

        @Override
        public Long read(JsonReader in) throws IOException {
            return readLong(in);
        }
    };

    private LenientNumber() {}

    private static Long readLong(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        if (token == JsonToken.BOOLEAN) {
            return in.nextBoolean() ? 1L : 0L;
        }
        if (token != JsonToken.NUMBER && token != JsonToken.STRING) {
            in.skipValue();
            return null;
        }
        String raw = in.nextString().trim();
        if (raw.isEmpty()) return null;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException notWhole) {
            try {
                return (long) Double.parseDouble(raw);
            } catch (NumberFormatException notNumeric) {
                return null;
            }
        }
    }
}
