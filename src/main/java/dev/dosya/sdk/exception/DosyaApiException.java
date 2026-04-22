package dev.dosya.sdk.exception;

public class DosyaApiException extends DosyaException {

    private final int status;
    private final String errorMessage;
    private final String raw;

    public DosyaApiException(int status, String errorMessage) {
        this(status, errorMessage, null);
    }

    public DosyaApiException(int status, String errorMessage, String raw) {
        super("[" + status + "] " + errorMessage);
        this.status = status;
        this.errorMessage = errorMessage;
        this.raw = raw;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getRaw() {
        return raw;
    }
}
