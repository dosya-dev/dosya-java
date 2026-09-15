package dev.dosya.sdk.internal;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/** Reads a boolean from {@code true/false}, {@code 0/1} or {@code "true"/"1"}; null stays null. */
final class LenientBoolean extends TypeAdapter<Boolean> {

    static final LenientBoolean INSTANCE = new LenientBoolean();

    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
        if (value == null) out.nullValue();
        else out.value(value);
    }

    @Override
    public Boolean read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        switch (token) {
            case BOOLEAN:
                return in.nextBoolean();
            case NUMBER:
                return in.nextDouble() != 0;
            case STRING:
                String s = in.nextString();
                return "true".equalsIgnoreCase(s) || "1".equals(s);
            case NULL:
                in.nextNull();
                return null;
            default:
                in.skipValue();
                return null;
        }
    }
}
