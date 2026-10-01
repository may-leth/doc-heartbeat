package com.may.soul.doc_heartbeat.mapper;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.may.soul.doc_heartbeat.dto.DocumentResponse;
import com.may.soul.doc_heartbeat.dto.IndexedDocument;
import com.may.soul.doc_heartbeat.dto.SearchResultResponse;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(String fileName, String mimeType, String content){
        return new DocumentResponse(
                fileName,
                mimeType,
                content,
                content.length()
        );
    }

    public IndexedDocument toIndexedDocument(DocumentResponse response){
        return new IndexedDocument(
                response.fileName(),
                response.mimeType(),
                response.content(),
                response.contentLength()
        );
    }

    public SearchResultResponse toSearchResult(Hit<IndexedDocument> hit){
        String fileName = hit.source() != null ? hit.source().fileName() : "unknown";
        String mimeType = hit.source() != null ? hit.source().mimeType() : "unknown";
        String snippet = extractSnippet(hit);
        double score = hit.score() != null ? hit.score() : 0.0;

        return new SearchResultResponse(fileName, mimeType, snippet, score);
    }

    private String extractSnippet(Hit<IndexedDocument> hit){
        if (hit.highlight() != null && hit.highlight().containsKey("content")) {
            return String.join(" ... ", hit.highlight().get("content"));
        }
        return "";
    }
}