package com.may.soul.doc_heartbeat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single search result matching the query")
public record SearchResultResponse(
        @Schema(description = "Name of the matching file", example = "report.pdf")
        String fileName,

        @Schema(description = "MIME type of the matching file", example = "application/pdf")
        String mimeType,

        @Schema(description = "Highlighted snippet of the content where the match was found")
        String highlightedSnippet,

        @Schema(description = "Relevance score assigned by Elasticsearch", example = "1.23")
        double score
) {}