package com.may.soul.doc_heartbeat.dto;

import com.may.soul.doc_heartbeat.mapper.DocumentMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class

DocumentMapperTest {

    private final DocumentMapper documentMapper = new DocumentMapper();

    @Test
    void toResponse_shouldBuildCorrectDocumentResponse() {
        DocumentResponse response = documentMapper.toResponse("file.pdf", "application/pdf", "contenido de prueba");

        assertEquals("file.pdf", response.fileName());
        assertEquals("application/pdf", response.mimeType());
        assertEquals("contenido de prueba", response.content());
        assertEquals(19, response.contentLength());
    }

    @Test
    void toIndexedDocument_shouldCopyAllFieldsFromResponse(){
        DocumentResponse response = new DocumentResponse("file.pdf", "application/pdf", "sample content", 14);

        assertEquals("file.pdf", response.fileName());
        assertEquals("application/pdf", response.mimeType());
        assertEquals("sample content", response.content());
        assertEquals(14, response.contentLength());
    }
}