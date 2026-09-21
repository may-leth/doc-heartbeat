package com.may.soul.doc_heartbeat.controller;

import com.may.soul.doc_heartbeat.dto.DocumentMapper;
import com.may.soul.doc_heartbeat.dto.DocumentResponse;
import com.may.soul.doc_heartbeat.exception.DocumentProcessingException;
import com.may.soul.doc_heartbeat.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private DocumentMapper documentMapper;

    @Test
    void uploadDocument_withValidTextFile_shouldReturnExtractedContent() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hola, este es un documento de prueba".getBytes()
        );

        String expectedContent = "Hola, este es un documento de prueba";
        String expectedMimeType = "text/plain";

        when(documentService.detectType(any())).thenReturn(expectedMimeType);
        when(documentService.extractContent(any())).thenReturn(expectedContent);
        when(documentMapper.toResponse(anyString(), anyString(), anyString()))
                .thenReturn(new DocumentResponse("test.txt", expectedMimeType, expectedContent, expectedContent.length()));

        mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.txt"))
                .andExpect(jsonPath("$.mimeType").value(expectedMimeType))
                .andExpect(jsonPath("$.content").value(expectedContent))
                .andExpect(jsonPath("$.contentLength").value(36));
    }

    @Test
    void uploadDocument_withoutFile_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/documents"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadDocument_whenServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "corrupted.pdf", "application/pdf",
                "contenido corrupto".getBytes()
        );

        when(documentService.detectType(any())).thenReturn("application/pdf");
        when(documentService.extractContent(any()))
                .thenThrow(new DocumentProcessingException("No se pudo parsear el documento"));

        mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void uploadDocument_withEmptyFile_shouldStillProcessAndReturnEmptyContent() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]
        );

        when(documentService.detectType(any())).thenReturn("text/plain");
        when(documentService.extractContent(any())).thenReturn("");
        when(documentMapper.toResponse(anyString(), anyString(), anyString()))
                .thenReturn(new DocumentResponse("empty.txt", "text/plain", "", 0));

        mockMvc.perform(multipart("/api/documents").file(emptyFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(""))
                .andExpect(jsonPath("$.contentLength").value(0));
    }
}
