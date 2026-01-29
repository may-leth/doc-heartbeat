package com.may.soul.doc_heartbeat.service;

import com.may.soul.doc_heartbeat.exception.DocumentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class DocumentService {
    private final Tika tika;

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final int MAX_STRING_LENGTH = 10_000_000; // 10MB

    public DocumentService() {
        this.tika = new Tika();
        this.tika.setMaxStringLength(MAX_STRING_LENGTH);
    }

    public String extractContent(InputStream inputStream){
        if (inputStream == null) {
            throw new IllegalArgumentException("El InputStream no puede ser null");
        }

        try (BufferedInputStream bufferedStream = new BufferedInputStream(inputStream, DEFAULT_BUFFER_SIZE)) {
            log.debug("Iniciando extracción de contenido del documento");

            String content = tika.parseToString(bufferedStream);

            log.info("Contenido extraído exitosamente. Longitud: {} caracteres", content.length());
            return content;
        } catch (TikaException exception) {
            log.error("Error de Tika al procesar el documento: {}", exception.getMessage(), exception);
            throw new DocumentProcessingException("No se pudo parsear el documento", exception);
        } catch (IOException exception) {
            log.error("Error de I/O al leer el documento: {}", exception.getMessage(), exception);
            throw new DocumentProcessingException("Error de lectura del documento", exception);
        }
    }

    public String detectType(InputStream inputStream) {
        if (inputStream == null) {
            log.warn("InputStream null recibido en detectType, retornando tipo por defecto");
            return DEFAULT_MIME_TYPE;
        }

        try (BufferedInputStream bufferedStream = new BufferedInputStream(inputStream, DEFAULT_BUFFER_SIZE)) {
            String mimeType = tika.detect(bufferedStream);

            log.debug("Tipo MIME detectado: {}", mimeType);
            return mimeType != null ? mimeType : DEFAULT_MIME_TYPE;
        } catch (IOException exception) {
            log.error("Error al detectar tipo MIME: {}", exception.getMessage(), exception);
            return DEFAULT_MIME_TYPE;
        }
    }
}