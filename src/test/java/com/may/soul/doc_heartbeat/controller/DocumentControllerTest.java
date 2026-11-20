package com.may.soul.doc_heartbeat.controller;

import com.may.soul.doc_heartbeat.dto.SearchResultResponse;
import com.may.soul.doc_heartbeat.mapper.DocumentMapper;
import com.may.soul.doc_heartbeat.dto.DocumentResponse;
import com.may.soul.doc_heartbeat.exception.DocumentProcessingException;
import com.may.soul.doc_heartbeat.service.DocumentService;
import com.may.soul.doc_heartbeat.service.SearchIndexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@SpringBootTest
@AutoConfigureMockMvc
public class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private DocumentMapper documentMapper;

    @MockitoBean
    private SearchIndexService searchIndexService;

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

        verify(searchIndexService).index(any());
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

    @Test
    void search_withMatchingQuery_shouldReturnResults() throws Exception {
        List<SearchResultResponse> results = List.of(
                new SearchResultResponse("acuerdo.pdf", "application/pdf",
                        "Acuerdo de <mark>confidencialidad</mark>", 0.37)
        );
        when(searchIndexService.search("confidencialidad")).thenReturn(results);

        mockMvc.perform(get("/api/documents/search").param("q", "confidencialidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fileName").value("acuerdo.pdf"))
                .andExpect(jsonPath("$[0].highlightedSnippet").value("Acuerdo de <mark>confidencialidad</mark>"));
    }

    @Test
    void search_withNoMatches_shouldReturnEmptyList() throws Exception {
        when(searchIndexService.search("inexistente")).thenReturn(List.of());

        mockMvc.perform(get("/api/documents/search").param("q", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_whenElasticsearchFails_shouldReturnInternalServerError() throws Exception {
        when(searchIndexService.search(anyString()))
                .thenThrow(new DocumentProcessingException("No se pudo realizar la búsqueda"));

        mockMvc.perform(get("/api/documents/search").param("q", "confidencialidad"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void search_withoutQueryParam_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/documents/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_withBlankQuery_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/documents/search").param("q", "   "))
                .andExpect(status().isBadRequest());

        verify(searchIndexService, never()).search(anyString());
    }

    @Test
    void search_withSpecialCharacters_shouldPassQueryUnchanged() throws Exception {
        String query = "cesión & imágenes";
        when(searchIndexService.search(query)).thenReturn(List.of());

        mockMvc.perform(get("/api/documents/search").param("q", query))
                .andExpect(status().isOk());

        verify(searchIndexService).search(query);
    }
}
