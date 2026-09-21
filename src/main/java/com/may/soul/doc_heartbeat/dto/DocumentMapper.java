package com.may.soul.doc_heartbeat.dto;

import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(String fileName, String mimeType, String content){
        return new DocumentResponse(
                fileName,
                mimeType,
                content,
                content.length()
        );
    }
}