package com.may.soul.doc_heartbeat.dto;

public record IndexedDocument(
        String fileName,
        String mimeType,
        String content,
        int contentLength
) {}