package com.may.soul.doc_heartbeat.dto;

public record DocumentResponse(
        String fileName,
        String mimeType,
        String content,
        int contentLength
) {}