package com.may.soul.doc_heartbeat.exception;

public class DocumentProcessingException extends RuntimeException {
    public DocumentProcessingException(String message) {
        super(message);
    }
    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
