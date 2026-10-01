package com.may.soul.doc_heartbeat.controller;

import com.may.soul.doc_heartbeat.mapper.DocumentMapper;
import com.may.soul.doc_heartbeat.dto.DocumentResponse;
import com.may.soul.doc_heartbeat.service.DocumentService;
import com.may.soul.doc_heartbeat.service.SearchIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Document content ingestion and extraction")
public class DocumentController  {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final SearchIndexService searchIndexService;

    public DocumentController(DocumentService documentService, DocumentMapper documentMapper, SearchIndexService searchIndexService) {
        this.documentService = documentService;
        this.documentMapper = documentMapper;
        this.searchIndexService = searchIndexService;
    }

    @Operation(
            summary = "Upload a document and extract its content.",
            description = "Analyze the file with Apache Tika (MIME type detection and plain text extraction)"
                    + "and indexes the result in Elasticsearch so it becomes searchable."
    )
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Document processed successfully"),
            @ApiResponse(responseCode = "400", description = "No file was sent in the request."),
            @ApiResponse(responseCode = "500", description = "Processing or indexing failed (unsupported format, corrupted file or Elasticsearch unavailable)")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @Parameter(
                    description = "file to process",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Recibida petición de subida: {}", file.getOriginalFilename());

        String mimeType;
        String content;

        try(InputStream mimeStream = file.getInputStream()){
            mimeType = documentService.detectType(mimeStream);
        }

        try(InputStream contentStream = file.getInputStream()){
            content = documentService.extractContent(contentStream);
        }

        DocumentResponse response = documentMapper.toResponse(file.getOriginalFilename(), mimeType, content);
        searchIndexService.index(documentMapper.toIndexedDocument(response));
        return ResponseEntity.ok(response);
    }
}