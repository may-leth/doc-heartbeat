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
* **Performance:** Reusing the instance improves performance by approximately **6x** (decreased latency from ~30s to ~4.7s in load tests).
* **Evidence:** A benchmark test (`TikaPerformanceTest`) is included in the test suite to verify these metrics.

## Roadmap 🚀
- [x] **Phase 1:** Project Setup & Tika Integration (Current).
- [ ] **Phase 2:** REST API development for file upload.
- [ ] **Phase 3:** Integration with **Elasticsearch** to index the extracted content.
- [ ] **Phase 4:** Dockerization of the complete pipeline (App + ES Node).