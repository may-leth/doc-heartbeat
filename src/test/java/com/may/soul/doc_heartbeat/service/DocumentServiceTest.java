package com.may.soul.doc_heartbeat.service;

import com.may.soul.doc_heartbeat.exception.DocumentProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentServiceTest {

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

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
    @DisplayName("Should wrap read errors in DocumentProcessingException")
    void extractContent_whenStreamFailsToRead_shouldThrowDocumentProcessingException() {
        assertThrows(DocumentProcessingException.class,
                () -> documentService.extractContent(failingStream()));
    }

    @Test
    @DisplayName("Should wrap parsing errors of a corrupted file in DocumentProcessingException")
    void extractContent_withCorruptedPdf_shouldThrowDocumentProcessingException() {
        byte[] corruptedPdf = "%PDF-1.4\n...contenido basura...".getBytes(StandardCharsets.UTF_8);

        assertThrows(DocumentProcessingException.class,
                () -> documentService.extractContent(new ByteArrayInputStream(corruptedPdf)));
    }

    @Test
    @DisplayName("Should detect PDF MIME type from magic bytes (Memory)")
    void detectType_withPdfHeader_shouldReturnPdfMime() {
        byte[] fakePdfData = "%PDF-1.4\n...contenido basura...".getBytes(StandardCharsets.UTF_8);
        InputStream memoryStream = new ByteArrayInputStream(fakePdfData);

        String mimeType = documentService.detectType(memoryStream);

        assertEquals("application/pdf", mimeType);
    }

    @Test
    @DisplayName("Should return default MIME type when stream is null")
    void detectType_withNullStream_shouldReturnDefaultMimeType() {
        assertEquals(DEFAULT_MIME_TYPE, documentService.detectType(null));
    }

    @Test
    @DisplayName("Should return default MIME type when stream fails to read")
    void detectType_whenStreamFailsToRead_shouldReturnDefaultMimeType() {
        assertEquals(DEFAULT_MIME_TYPE, documentService.detectType(failingStream()));
    }

    private static InputStream failingStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated read failure");
            }
        };
    }
}