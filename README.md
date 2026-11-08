# Spring AI Demo: Intelligent RAG System 

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.6-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.2-blue)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)

A professional-grade implementation of **Retrieval-Augmented Generation (RAG)** using the Spring AI framework. This project demonstrates how to integrate Large Language Models (LLMs) like **Google Gemini** with local vector storage to create context-aware AI applications.

## 🌟 Key Features

* **RAG Architecture:** Leverages semantic search to provide the LLM with custom context.
* **Vector Store Integration:** Implements **ChromaDB** for efficient document embedding and retrieval.
* **Multi-Model Support:** Configured for Google Gemini (via AI Studio) with modular support for OpenAI and DeepSeek.
* **RESTful API:** Clean `AiController` endpoints for interacting with the AI service.
* **Design Patterns:** Utilizes Factory and Service patterns for scalable AI orchestration.

---

## 🏗️ Architecture Flow

1.  **Ingestion:** Documents are loaded and split into chunks.
2.  **Embedding:** Text chunks are converted into high-dimensional vectors using `text-embedding-004`.
3.  **Storage:** Vectors are stored in a Vector Database (Chroma).
4.  **Retrieval:** At query time, the system finds the most relevant document chunks.
5.  **Generation:** The LLM generates a response based on the original query + retrieved context.

---

## 🛠️ Tech Stack

* **Backend:** Java 17, Spring Boot 3.3.6
* **AI Framework:** Spring AI 1.1.2
* **LLM Provider:** Google Gemini API
* **Embeddings:** Google GenAI Embeddings
* **Vector Store:** ChromaDB
* **Build Tool:** Maven

---

## 🚀 Getting Started

### Prerequisites

* JDK 17 or higher
* Maven 3.x
* A Google AI Studio API Key

### Configuration

Add your API key to your environment variables:

```bash
# Windows (Command Prompt)
setx GOOGLE_API_KEY "your_api_key_here"

# Linux / MacOS
export GOOGLE_API_KEY="your_api_key_here"
```

Update your `src/main/resources/application.properties`:

```properties
spring.application.name=spring-ai-demo
spring.ai.google.genai.api-key=${GOOGLE_API_KEY}
# Port
server.port=8081
```

### Running the Project

```bash
mvn clean spring-boot:run
```

---

## 📂 Project Structure

```text
src/main/java/com/cm/spring_ai_demo/
├── config/             # AiConfig (VectorStore, ChatMemory, TokenTextSplitter)
├── controller/         # AiController (General Chat) & RagController (PDF RAG)
├── dto/                # DocumentInfo, RagResponse
├── service/            # GeminiService & RagService (PDF parsing, chunking, semantic search)
└── SpringAiDemoApplication.java
```

---

## 📑 PDF RAG API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/ai/rag/upload` | Upload PDF file (multipart/form-data) for text extraction, chunking, and vector indexing |
| `GET` | `/api/ai/rag/chat?userId=...&message=...` | Grounded semantic search Q&A with source citations (file + page) |
| `GET` | `/api/ai/rag/documents` | List all currently indexed PDF documents |
| `DELETE` | `/api/ai/rag/clear` | Clear the vector store and reset indexed documents |
| `GET` | `/api/ai/chat?userId=...&message=...` | General LLM conversation with multi-turn chat memory |

---

### 💡 OmniAI Pro-Tip:
* **Stateful Sessions:** OmniAI uses Spring AI's `MessageChatMemoryAdvisor` combined with client-side persistence to maintain multi-turn context across reloads.
* **Virtual Threads for Concurrency:** `spring.threads.virtual.enabled=true` is enabled in `application.properties` to allow OmniAI to handle thousands of concurrent AI conversations with near-zero latency overhead.
* **Model Compatibility:** Configured for `gemini-3.1-flash-lite-preview` on Google GenAI SDK for the best price-performance ratio.

---

## 👨‍💻 Project & Author

**OmniAI**
* Intelligent Spring AI Assistant & RAG Architecture
* Powered by Spring Boot 3.5, Spring AI 1.1.2 & Google GenAI

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/4a28da7b-5f07-43d5-b33a-94846e341bbc" />
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/605668ef-5941-4376-ad61-6f3d806eb912" />
