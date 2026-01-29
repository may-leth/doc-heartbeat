# Document Heartbeat 🫀
> **"Listening to what your files have to say."**

## Project Goal
This project constitutes the **ingestion layer** of a search architecture. It leverages **Spring Boot 4** and **Apache Tika 3.2.3** to transform unstructured binary data (PDFs, Images, Office docs) into structured, index-ready JSON formats.

## Technical Scope & Stack
* **Extraction:** Apache Tika (MIME detection, OCR, Metadata extraction).
* **Processing:** Java 21 + Spring Boot 4.
* **Target Integration:** Designed to feed extracted data into **Elasticsearch** for full-text search capabilities.

## Roadmap 🚀
- [x] **Phase 1:** Project Setup & Tika Integration (Current).
- [ ] **Phase 2:** REST API development for file upload.
- [ ] **Phase 3:** Integration with **Elasticsearch** to index the extracted content.
- [ ] **Phase 4:** Dockerization of the complete pipeline (App + ES Node).