package com.may.soul.doc_heartbeat.mapper;

import com.may.soul.doc_heartbeat.dto.DocumentResponse;
import com.may.soul.doc_heartbeat.dto.IndexedDocument;
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

    public IndexedDocument toIndexedDocument(DocumentResponse response){
        return new IndexedDocument(
                response.fileName(),
                response.mimeType(),
                response.content(),
                response.contentLength()
        );
    }
}