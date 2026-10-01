package com.may.soul.doc_heartbeat.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.may.soul.doc_heartbeat.dto.IndexedDocument;
import com.may.soul.doc_heartbeat.exception.DocumentProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchIndexServiceTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Test
    void index_withValidDocument_shouldCallElasticsearchClientSuccessfully() throws IOException {
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient);

        IndexedDocument document = new IndexedDocument("file.pdf", "application/pdf", "sample content", 14);

        IndexResponse mockResponse = mock(IndexResponse.class);
        when(mockResponse.id()).thenReturn("abc123");
        when(elasticsearchClient.<IndexedDocument>index(any(Function.class))).thenReturn(mockResponse);

        searchIndexService.index(document);

        verify(elasticsearchClient).<IndexedDocument>index(any(Function.class));
    }

    @Test
    void index_whenElasticsearchThrowsIOException_shouldThrowDocumentProcessingException() throws IOException {
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient);

        IndexedDocument document = new IndexedDocument("file.pdf", "application/pdf", "sample content", 14);

        when(elasticsearchClient.<IndexedDocument>index(any(Function.class)))
                .thenThrow(new IOException("Connection refused"));

        DocumentProcessingException exception = assertThrows(
                DocumentProcessingException.class,
                () -> searchIndexService.index(document)
        );

        assertEquals("No se pudo indexar el documento", exception.getMessage());
    }

    @Test
    void index_whenElasticsearchThrowsUnexpectedRuntimeException_shouldPropagateIt() throws IOException {
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient);

        IndexedDocument document = new IndexedDocument("file.pdf", "application/pdf", "sample content", 14);

        when(elasticsearchClient.<IndexedDocument>index(any(Function.class)))
                .thenThrow(new RuntimeException("Unexpected client error"));

        assertThrows(RuntimeException.class, () -> searchIndexService.index(document));
    }

    @Test
    void index_withNullDocument_shouldThrowException() {
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient);

        assertThrows(NullPointerException.class, () -> searchIndexService.index(null));
    }
}