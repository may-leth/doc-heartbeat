package com.may.soul.doc_heartbeat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DocumentServiceTest {

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService();
    }

    @Test
    @DisplayName("Should extract content from a generated memory stream (Fast)")
    void extractContent_withMemoryStream_shouldReturnText() {
        String fakeContent = "Este es un contenido de prueba simulando un archivo.";
        InputStream memoryStream = new ByteArrayInputStream(fakeContent.getBytes(StandardCharsets.UTF_8));

        String content = documentService.extractContent(memoryStream);

        assertNotNull(content);
        assertTrue(content.contains("contenido de prueba"));
    }

    @Test
    @DisplayName("Should throw exception when stream is null")
    void extractContent_withNullStream_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> documentService.extractContent(null));
    }

    @Test
    @DisplayName("Should detect PDF MIME type from magic bytes (Memory)")
    void detectType_withPdfHeader_shouldReturnPdfMime() {
        byte[] fakePdfData = "%PDF-1.4\n...contenido basura...".getBytes(StandardCharsets.UTF_8);
        InputStream memoryStream = new ByteArrayInputStream(fakePdfData);

        String mimeType = documentService.detectType(memoryStream);

        assertEquals("application/pdf", mimeType);
    }
}