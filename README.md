# ⚡ DocuSense AI — Enterprise Document Intelligence & Semantic Q&A System

> **Architecture:** Java 21, Spring Boot 3, Hibernate (Spring Data JPA), Spring AI, PostgreSQL (pgvector) / SimpleVectorStore, Apache PDFBox, and Server-Sent Events (SSE).

---

## 🌟 Overview
**DocuSense AI** is an enterprise-grade document intelligence system implementing a complete **Retrieval-Augmented Generation (RAG)** pipeline. Users can upload complex unstructured documents (PDFs, text files), which are parsed, split into contextual chunks, converted into high-dimensional vector embeddings, and indexed into a vector store. Users can then converse with the system to ask complex questions, receiving accurate, hallucination-free answers with precise document citations.

---

## 🏗️ Architecture & Component Flow

```
                      +-----------------------------+
                      |   DocuSense Web Dashboard   |
                      |  (HTML5 / CSS / Vanilla JS) |
                      +--------------+--------------+
                                     |  REST APIs / SSE
                                     v
+-------------------------------------------------------------------------+
|                        SPRING BOOT 3 BACKEND                            |
|                                                                         |
|  [DocumentController]                      [ChatController]             |
|          |                                         |                    |
|          v                                         v                    |
|  [DocumentService]                          [RagChatService]            |
|    - Apache PDFBox Text Extraction            - Context Augmentation    |
|    - Recursive Text Chunking                  - Spring AI ChatClient    |
|          |                                         |                    |
|          +-------------------+---------------------+                    |
|                              |                                          |
|                              v                                          |
|                    [VectorStoreService]                                 |
|            (Embedding Model + Similarity Search)                        |
+------------------------------+------------------------------------------+
                               |
            +------------------+------------------+
            |                                     |
            v                                     v
+-------------------------+             +-------------------------+
|     HIBERNATE / JPA     |             |      VECTOR STORE       |
|    (Relational Data)    |             |  (High-Dim Embeddings)  |
|                         |             |                         |
| • Users Table           |             | • SimpleVectorStore     |
| • Documents Table       |             |   (In-Memory / File)    |
| • Chat Sessions Table   |             | • PostgreSQL + pgvector |
| • Chat Messages Table   |             |   (Production Mode)     |
+-------------------------+             +-------------------------+
```

---

## 🚀 Key Features

1. **End-to-End RAG Pipeline:**
   - Text parsing using **Apache PDFBox 3**.
   - Recursive chunking with token overlap to maintain cross-boundary context.
   - Vector indexing using **Spring AI VectorStore**.
   - Semantic similarity search retrieving Top-K candidate chunks for each user query.

2. **Advanced Hibernate (Spring Data JPA) Relational Mapping:**
   - `UserEntity`: User identities and roles.
   - `DocumentEntity`: File metadata, processing state (`PROCESSING`, `INDEXED`, `FAILED`), chunk counts.
   - `ChatSessionEntity`: Multi-turn conversational sessions.
   - `ChatMessageEntity`: Detailed prompt logs and serialized retrieved source citations.

3. **Hybrid Database Design:**
   - **Default Mode:** In-memory **H2 Database** with web console at `/h2-console` for zero-setup execution.
   - **Production Mode:** **PostgreSQL with pgvector** via Docker container (`docker-compose.yml`).

4. **Real-time Server-Sent Events (SSE):**
   - Live token-by-token streaming response for a responsive ChatGPT-like user experience.

5. **Interactive Single-Page Application (SPA):**
   - Built into `src/main/resources/static/` with no Node/NPM build step required!

