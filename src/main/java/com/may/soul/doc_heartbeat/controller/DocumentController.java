package com.may.soul.doc_heartbeat.controller;

import com.may.soul.doc_heartbeat.dto.DocumentMapper;
import com.may.soul.doc_heartbeat.dto.DocumentResponse;
import com.may.soul.doc_heartbeat.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
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
public class DocumentController  {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;

    public DocumentController(DocumentService documentService, DocumentMapper documentMapper) {
        this.documentService = documentService;
        this.documentMapper = documentMapper;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
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
        return ResponseEntity.ok(response);
    }
}