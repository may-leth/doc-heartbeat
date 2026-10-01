package com.may.soul.doc_heartbeat.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.may.soul.doc_heartbeat.dto.IndexedDocument;
import com.may.soul.doc_heartbeat.exception.DocumentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class SearchIndexService {

    private static final String INDEX_NAME = "documents";

    private final ElasticsearchClient elasticsearchClient;

    public SearchIndexService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
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
}