package com.may.soul.doc_heartbeat.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.may.soul.doc_heartbeat.dto.IndexedDocument;
import com.may.soul.doc_heartbeat.dto.SearchResultResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class SearchIndexServiceIntegrationTest {

    private static final String INDEX_NAME = "documents";

    @Container
    static ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:9.2.3")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .withStartupTimeout(Duration.ofMinutes(3));

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("elasticsearch.uri", () -> "http://" + elasticsearch.getHttpHostAddress());
    }

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @BeforeEach
    void cleanIndex() throws IOException {
        elasticsearchClient.indices().delete(d -> d.index(INDEX_NAME).ignoreUnavailable(true));
    }

    @Test
    @DisplayName("Should find an indexed document and highlight the match")
    void search_afterIndexing_shouldFindDocumentWithHighlight() throws IOException {
        indexAndRefresh(new IndexedDocument("acuerdo.pdf", "application/pdf",
                "Acuerdo de confidencialidad entre las partes", 44));

        List<SearchResultResponse> results = searchIndexService.search("confidencialidad");

        assertEquals(1, results.size());
        assertEquals("acuerdo.pdf", results.getFirst().fileName());
        assertTrue(results.getFirst().highlightedSnippet().contains("<mark>confidencialidad</mark>"));
        assertTrue(results.getFirst().score() > 0);
    }

    @Test
    @DisplayName("Should rank the most relevant document first")
    void search_withSeveralMatches_shouldRankMostRelevantFirst() throws IOException {
        indexAndRefresh(
                new IndexedDocument("novela.epub", "application/epub+zip",
                        "Una larga historia sobre viajes, familia y un breve compromiso de confidencialidad", 82),
                new IndexedDocument("acuerdo.pdf", "application/pdf",
                        "Confidencialidad. Deber de confidencialidad. La confidencialidad se mantiene", 75)
        );

        List<SearchResultResponse> results = searchIndexService.search("confidencialidad");

        assertEquals(2, results.size());
        assertEquals("acuerdo.pdf", results.getFirst().fileName());
    }

    @Test
    @DisplayName("Should return an empty list when nothing matches")
    void search_withNoMatches_shouldReturnEmptyList() throws IOException {
        indexAndRefresh(new IndexedDocument("receta.txt", "text/plain", "Tortilla de patatas con cebolla", 31));

        List<SearchResultResponse> results = searchIndexService.search("confidencialidad");

        assertTrue(results.isEmpty());
    }

    private void indexAndRefresh(IndexedDocument... documents) throws IOException {
        for (IndexedDocument document : documents) {
            searchIndexService.index(document);
        }
        elasticsearchClient.indices().refresh(r -> r.index(INDEX_NAME));
    }
}