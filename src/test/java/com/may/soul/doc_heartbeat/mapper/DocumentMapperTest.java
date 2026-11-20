package com.may.soul.doc_heartbeat.mapper;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.may.soul.doc_heartbeat.dto.DocumentResponse;
import com.may.soul.doc_heartbeat.dto.IndexedDocument;
import com.may.soul.doc_heartbeat.dto.SearchResultResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentMapperTest {

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
    void toIndexedDocument_shouldCopyAllFieldsFromResponse() {
        DocumentResponse response = new DocumentResponse("file.pdf", "application/pdf", "sample content", 14);

        IndexedDocument indexed = documentMapper.toIndexedDocument(response);

        assertEquals("file.pdf", indexed.fileName());
        assertEquals("application/pdf", indexed.mimeType());
        assertEquals("sample content", indexed.content());
        assertEquals(14, indexed.contentLength());
    }

    @Test
    void toSearchResult_shouldBuildCorrectSearchResultResponse() {
        IndexedDocument indexedDocument = new IndexedDocument("file.pdf", "application/pdf", "full content here", 18);

        Hit<IndexedDocument> hit = Hit.of(h -> h
                .index("documents")
                .id("abc123")
                .score(1.5)
                .source(indexedDocument)
                .highlight(Map.of("content", List.of("...matched <mark>content</mark>...")))
        );

        SearchResultResponse result = documentMapper.toSearchResult(hit);

        assertEquals("file.pdf", result.fileName());
        assertEquals("application/pdf", result.mimeType());
        assertEquals("...matched <mark>content</mark>...", result.highlightedSnippet());
        assertEquals(1.5, result.score());
    }

    @Test
    void toSearchResult_withMultipleHighlights_shouldJoinThemWithSeparator() {
        IndexedDocument indexedDocument = new IndexedDocument("file.pdf", "application/pdf", "content", 7);

        Hit<IndexedDocument> hit = Hit.of(h -> h
                .index("documents")
                .id("abc123")
                .score(1.0)
                .source(indexedDocument)
                .highlight(Map.of("content", List.of("first <mark>match</mark>", "second <mark>match</mark>")))
        );

        SearchResultResponse result = documentMapper.toSearchResult(hit);

        assertEquals("first <mark>match</mark> ... second <mark>match</mark>", result.highlightedSnippet());
    }

    @Test
    void toSearchResult_withoutHighlight_shouldReturnEmptySnippet() {
        IndexedDocument indexedDocument = new IndexedDocument("file.pdf", "application/pdf", "content", 7);

        Hit<IndexedDocument> hit = Hit.of(h -> h
                .index("documents")
                .id("abc123")
                .score(1.0)
                .source(indexedDocument)
        );

        SearchResultResponse result = documentMapper.toSearchResult(hit);

        assertEquals("", result.highlightedSnippet());
    }

    @Test
    void toSearchResult_withoutSource_shouldUseUnknownValues() {
        Hit<IndexedDocument> hit = Hit.of(h -> h
                .index("documents")
                .id("abc123")
                .score(1.0)
        );

        SearchResultResponse result = documentMapper.toSearchResult(hit);

        assertEquals("unknown", result.fileName());
        assertEquals("unknown", result.mimeType());
    }

    @Test
    void toSearchResult_withoutScore_shouldDefaultToZero() {
        IndexedDocument indexedDocument = new IndexedDocument("file.pdf", "application/pdf", "content", 7);

        Hit<IndexedDocument> hit = Hit.of(h -> h
                .index("documents")
                .id("abc123")
                .source(indexedDocument)
        );

        SearchResultResponse result = documentMapper.toSearchResult(hit);

        assertEquals(0.0, result.score());
    }
}