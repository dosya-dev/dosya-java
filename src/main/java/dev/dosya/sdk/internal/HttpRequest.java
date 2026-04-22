package dev.dosya.sdk.internal;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpRequest {

    private final String method;
    private final String path;
    private Map<String, String> query;
    private Object body;
    private byte[] rawBody;
    private Map<String, String> headers;
    private boolean rawResponse;

    public HttpRequest(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public static HttpRequest get(String path) {
        return new HttpRequest("GET", path);
    }

    public static HttpRequest post(String path) {
        return new HttpRequest("POST", path);
    }

    public static HttpRequest put(String path) {
        return new HttpRequest("PUT", path);
    }

    public static HttpRequest delete(String path) {
        return new HttpRequest("DELETE", path);
    }

    public HttpRequest query(String key, Object value) {
        if (value != null) {
            if (query == null) {
                query = new LinkedHashMap<String, String>();
            }
            query.put(key, String.valueOf(value));
        }
        return this;
    }

    public HttpRequest body(Object body) {
        this.body = body;
        return this;
    }

    public HttpRequest rawBody(byte[] rawBody) {
        this.rawBody = rawBody;
        return this;
    }

    public HttpRequest header(String key, String value) {
        if (headers == null) {
            headers = new LinkedHashMap<String, String>();
        }
        headers.put(key, value);
        return this;
    }

    public HttpRequest rawResponse(boolean rawResponse) {
        this.rawResponse = rawResponse;
        return this;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getQuery() { return query; }
    public Object getBody() { return body; }
    public byte[] getRawBody() { return rawBody; }
    public Map<String, String> getHeaders() { return headers; }
    public boolean isRawResponse() { return rawResponse; }
}
