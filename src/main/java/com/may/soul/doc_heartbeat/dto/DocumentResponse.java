package com.may.soul.doc_heartbeat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of content extraction from a document")
public record DocumentResponse(
        @Schema(description = "Original name of the uploaded file", example = "informe.pdf")
        String fileName,

        @Schema(description = "Detected MIME type", example = "application/pdf")
        String mimeType,

        @Schema(description = "Text extracted from the document")
        String content,

        @Schema(description = "Length of the extracted text in characters", example = "1523")
        int contentLength
) {}