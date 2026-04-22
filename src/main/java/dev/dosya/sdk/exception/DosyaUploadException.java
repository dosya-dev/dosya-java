package dev.dosya.sdk.exception;

public class DosyaUploadException extends DosyaException {

    private final String sessionId;
    private final Integer partNumber;

    public DosyaUploadException(String message, String sessionId) {
        this(message, sessionId, null);
    }

    public DosyaUploadException(String message, String sessionId, Integer partNumber) {
        super(message);
        this.sessionId = sessionId;
        this.partNumber = partNumber;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Integer getPartNumber() {
        return partNumber;
    }
}
