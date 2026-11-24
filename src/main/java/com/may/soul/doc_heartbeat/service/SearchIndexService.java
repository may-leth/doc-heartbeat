package com.may.soul.doc_heartbeat.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.util.NamedValue;
import com.may.soul.doc_heartbeat.dto.IndexedDocument;
import com.may.soul.doc_heartbeat.dto.SearchResultResponse;
import com.may.soul.doc_heartbeat.exception.DocumentProcessingException;
import com.may.soul.doc_heartbeat.mapper.DocumentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class SearchIndexService {

    private static final String INDEX_NAME = "documents";

    private final ElasticsearchClient elasticsearchClient;
    private final DocumentMapper documentMapper;
    private static final String INDEX_NOT_FOUND = "index_not_found_exception";

    public SearchIndexService(ElasticsearchClient elasticsearchClient, DocumentMapper documentMapper) {
        this.elasticsearchClient = elasticsearchClient;
        this.documentMapper = documentMapper;
    }

    public void index(IndexedDocument document) {
        try {
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index(INDEX_NAME)
                    .document(document)
            );

            log.info("Documento indexado correctamente. ID: {}, Resultado: {}", response.id(), response.result());

        } catch (IOException exception) {
            log.error("Error al indexar el documento en Elasticsearch: {}", exception.getMessage(), exception);
            throw new DocumentProcessingException("No se pudo indexar el documento", exception);
        }
    }

    public List<SearchResultResponse> search(String query) {
        try {
            HighlightField highlightField = HighlightField.of(f -> f
                    .preTags("<mark>").postTags("</mark>")
            );

            SearchResponse<IndexedDocument> response = elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q.match(m -> m.field("content").query(query)))
                            .highlight(h -> h.fields(List.of(NamedValue.of("content", highlightField)))),
                    IndexedDocument.class
            );

            return response.hits().hits().stream()
                    .map(documentMapper::toSearchResult)
                    .toList();

        } catch (ElasticsearchException exception) {
            if (INDEX_NOT_FOUND.equals(exception.error().type())) {
                log.info("El índice '{}' aún no existe: todavía no hay documentos indexados", INDEX_NAME);
                return List.of();
            }
            log.error("Error de Elasticsearch al buscar documentos: {}", exception.getMessage(), exception);
            throw new DocumentProcessingException("No se pudo realizar la búsqueda", exception);

        } catch (IOException exception) {
            log.error("Error al buscar documentos en Elasticsearch: {}", exception.getMessage(), exception);
            throw new DocumentProcessingException("No se pudo realizar la búsqueda", exception);
        }
    }
}