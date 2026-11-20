package com.may.soul.doc_heartbeat.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.may.soul.doc_heartbeat.dto.IndexedDocument;
import com.may.soul.doc_heartbeat.dto.SearchResultResponse;
import com.may.soul.doc_heartbeat.exception.DocumentProcessingException;
import com.may.soul.doc_heartbeat.mapper.DocumentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchIndexServiceTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private DocumentMapper documentMapper;

    @Test
    void index_withValidDocument_shouldCallElasticsearchClientSuccessfully() throws IOException {
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient, documentMapper);

        IndexedDocument document = new IndexedDocument("file.pdf", "application/pdf", "sample content", 14);

        IndexResponse mockResponse = mock(IndexResponse.class);
        when(mockResponse.id()).thenReturn("abc123");
        when(elasticsearchClient.<IndexedDocument>index(any(Function.class))).thenReturn(mockResponse);

        searchIndexService.index(document);

        verify(elasticsearchClient).<IndexedDocument>index(any(Function.class));
    }

    @Test
    void index_whenElasticsearchThrowsIOException_shouldThrowDocumentProcessingException() throws IOException {
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient, documentMapper);

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
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient, documentMapper);

        IndexedDocument document = new IndexedDocument("file.pdf", "application/pdf", "sample content", 14);

        when(elasticsearchClient.<IndexedDocument>index(any(Function.class)))
                .thenThrow(new RuntimeException("Unexpected client error"));

        assertThrows(RuntimeException.class, () -> searchIndexService.index(document));
    }

    @Test
    void index_withNullDocument_shouldThrowException() {
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient, documentMapper);

        assertThrows(NullPointerException.class, () -> searchIndexService.index(null));
    }

    @Test
    void search_withValidQuery_shouldReturnMappedResults() throws IOException {
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient, documentMapper);

        SearchResponse<IndexedDocument> mockResponse = mock(SearchResponse.class);
        HitsMetadata<IndexedDocument> mockHitsMetadata = mock(HitsMetadata.class);
        Hit<IndexedDocument> mockHit = mock(Hit.class);

        when(mockResponse.hits()).thenReturn(mockHitsMetadata);
        when(mockHitsMetadata.hits()).thenReturn(List.of(mockHit));
        when(elasticsearchClient.<IndexedDocument>search(any(Function.class), eq(IndexedDocument.class)))
                .thenReturn(mockResponse);

        SearchResultResponse expectedResult = new SearchResultResponse("file.pdf", "application/pdf", "snippet", 1.2);
        when(documentMapper.toSearchResult(mockHit)).thenReturn(expectedResult);

        List<SearchResultResponse> results = searchIndexService.search("confidencialidad");

        assertEquals(1, results.size());
        assertEquals(expectedResult, results.get(0));
    }

    @Test
    void search_whenElasticsearchThrowsIOException_shouldThrowDocumentProcessingException() throws IOException {
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient, documentMapper);

        when(elasticsearchClient.<IndexedDocument>search(any(Function.class), eq(IndexedDocument.class)))
                .thenThrow(new IOException("Connection refused"));

        DocumentProcessingException exception = assertThrows(
                DocumentProcessingException.class,
                () -> searchIndexService.search("confidencialidad")
        );

        assertEquals("No se pudo realizar la búsqueda", exception.getMessage());
    }

    @Test
    void search_withNoMatches_shouldReturnEmptyList() throws IOException {
        SearchIndexService searchIndexService = new SearchIndexService(elasticsearchClient, documentMapper);

        SearchResponse<IndexedDocument> mockResponse = mock(SearchResponse.class);
        HitsMetadata<IndexedDocument> mockHitsMetadata = mock(HitsMetadata.class);

        when(mockResponse.hits()).thenReturn(mockHitsMetadata);
        when(mockHitsMetadata.hits()).thenReturn(List.of());
        when(elasticsearchClient.<IndexedDocument>search(any(Function.class), eq(IndexedDocument.class)))
                .thenReturn(mockResponse);

        List<SearchResultResponse> results = searchIndexService.search("palabra_inexistente");

        assertEquals(0, results.size());
    }
}