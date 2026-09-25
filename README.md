# Document Heartbeat 🫀
> **"Listening to what your files have to say."**

## Project Goal
This project constitutes the **ingestion layer** of a search architecture. It leverages **Spring Boot 4** and **Apache Tika 3.2.3** to transform unstructured binary data (PDFs, Images, Office docs) into structured, index-ready JSON formats.

## Technical Scope & Stack
* **Extraction:** Apache Tika (MIME detection, OCR, Metadata extraction).
* **Processing:** Java 21 + Spring Boot 4.
* **Target Integration:** Designed to feed extracted data into **Elasticsearch** for full-text search capabilities.

## Technical Decisions 🛠️
### Apache Tika Thread-Safety Strategy
A shared singleton instance of `Tika` is used within the `DocumentService` instead of instantiating per-request. This decision is based on:
* **Thread-Safety:** The basic parsing operations in Tika are thread-safe by design.
* **Performance:** Reusing the instance improves performance by approximately **7x** (~3.2s vs ~22.8s for 1000 sequential parses in local benchmark).
* **Evidence:** A benchmark test (`TikaPerformanceTest`) is included in the test suite to verify these metrics. Results may vary slightly between runs due to JVM JIT warmup and GC timing, but the relative improvement remains consistent.

### Centralized Error Handling
A `@RestControllerAdvice` (`GlobalExceptionHandler`) intercepts `DocumentProcessingException` thrown from the service layer, keeping the controller free of try/catch blocks and ensuring consistent, structured error responses across the API.

### API Documentation
The API is self-documented using **springdoc-openapi**, exposing an interactive Swagger UI at `/swagger-ui.html` and the raw OpenAPI spec at `/v3/api-docs`. This allows any consumer (frontend, QA, or another service) to explore and test endpoints without external tools.

## Try it out
Once the application is running (`./mvnw spring-boot:run`), explore the API interactively at:
`http://localhost:8080/swagger-ui.html`

## Roadmap 🚀
- [x] **Phase 1:** Project Setup & Tika Integration.
- [x] **Phase 2:** REST API for file upload, DTO/Mapper layer, centralized exception handling, and Swagger documentation.
- [ ] **Phase 3:** Integration with **Elasticsearch** to index the extracted content.
- [ ] **Phase 4:** Dockerization of the complete pipeline (App + ES Node).