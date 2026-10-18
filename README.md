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

---

## ⚙️ How to Run the Project

### 1. Prerequisites:
- Java 21 or Java 25 installed (`java -version`)
- Maven 3.9+ installed (`mvn -version`)

### 2. Run Locally (Zero Setup):
Navigate to the project root directory and start the Spring Boot application:

```bash
mvn spring-boot:run
```

Once started, open your web browser:
- **Web Application:** `http://localhost:8080/`
- **H2 Database Console:** `http://localhost:8080/h2-console`  
  *(JDBC URL: `jdbc:h2:mem:docusensedb`, Username: `sa`, Password: leave blank)*

### 3. Connect to Google Gemini / OpenAI (Optional):
To use live cloud LLM generation instead of the offline RAG generator:
- Set environment variable:
  ```bash
  export OPENAI_API_KEY="your-api-key-here"
  ```
  *(Or in Windows PowerShell: `$env:OPENAI_API_KEY="your-api-key-here"`)*

---

## 📡 REST API Documentation

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/documents/upload` | Upload PDF/text file, extract text, chunk & index vectors |
| `GET` | `/api/documents` | List all uploaded documents with processing status |
| `DELETE` | `/api/documents/{id}` | Delete document and related vector chunks |
| `POST` | `/api/chat/ask` | Send question, retrieve relevant chunks, query LLM |
| `GET` | `/api/chat/stream` | Server-Sent Events (SSE) streaming chat endpoint |
| `GET` | `/api/dashboard/stats` | System overview stats (total chunks, docs, messages) |
| `GET` | `/api/dashboard/inspect-database` | Inspect raw Hibernate entities |

---

## 🎯 Interview Talking Points for Budati Alekhya

When an interviewer asks you about this project:

> *"In **DocuSense AI**, I built an enterprise-grade document intelligence system using **Java 21, Spring Boot 3, Hibernate JPA, and Spring AI**.*
>
> *Rather than just making direct API calls, I designed a complete **RAG (Retrieval-Augmented Generation)** architecture:*
> 1. *Documents uploaded via REST endpoints are parsed using **Apache PDFBox** and split into overlapping semantic chunks.*
> 2. *These chunks are converted into vector embeddings using **Spring AI** and indexed in a vector store for semantic similarity search.*
> 3. *For the relational data layer, I used **Hibernate (Spring Data JPA)** to manage document audit states, user roles, and conversational chat sessions.*
> 4. *When answering questions, the system retrieves only the top-K relevant passages and augments the LLM prompt, virtually eliminating hallucinations and reducing token costs.*
> 5. *I also implemented **Server-Sent Events (SSE)** for real-time streaming tokens, giving users an interactive streaming experience."*
