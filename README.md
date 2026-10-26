
---

## 📡 API Contract

### Send a message (standard)
```http
POST /api/chat?sessionId={sessionId}
Content-Type: application/json

{
  "message": "What is Spring Boot?",
  "selectedFiles": []
}
```

### Send a message (streaming)
```http
POST /api/chat/stream?sessionId={sessionId}
Content-Type: application/json

{
  "message": "Explain RAG in detail",
  "selectedFiles": ["document.pdf"]
}
```

### Upload a PDF
```http
POST /api/documents/upload
Content-Type: multipart/form-data

files=<your PDF files>
sessionId=<your session ID>
```

### Delete a document
```http
DELETE /api/documents/{sessionId}/{fileName}
```

### Get chat sessions
```http
GET /api/chat/sessions
```

### Get messages for a session
```http
GET /api/chat/{sessionId}/messages
```

### Delete a session
```http
DELETE /api/chat/{sessionId}
```

### Rename a session
```http
PATCH /api/chat/{sessionId}/rename
Content-Type: application/json

{
  "title": "My new session name"
}
```

### Success Response
```json
{
  "message": "Spring Boot is a Java framework...",
  "sessionId": "session_abc123",
  "requestTime": "2026-10-11T13:00:00",
  "responseTime": "2026-10-11T13:00:02",
  "promptTokens": 65,
  "completionTokens": 20,
  "totalTokens": 85
}
```

### Error Response
```json
{
  "status": 500,
  "error": "Something went wrong",
  "path": "/api/chat",
  "timestamp": "2026-10-11T13:00:00"
}
```

---

## ⚙️ Configuration

| Property | Description | Default |
|---|---|---|
| `chatbot.memory.max-messages` | Max messages in conversation memory | 10 |
| `spring.ai.model.embedding` | Embedding provider | ollama |
| `spring.ai.ollama.embedding.model` | Ollama embedding model | nomic-embed-text |
| `spring.ai.vectorstore.chroma.collection-name` | Chroma collection name | chatbot-docs |
| `spring.data.redis.host` | Redis host | localhost |
| `spring.data.redis.port` | Redis port | 6380 |
| `spring.datasource.url` | PostgreSQL connection URL | jdbc:postgresql://localhost:5432/chatbotdb |

---

## 🧪 Running Tests

```bash
mvn test
```

4 unit tests covering the core service layer.

---

## 📚 What I Learned Building This

- Spring Boot REST API design and best practices
- Spring AI integration with multiple AI providers
- Multi-provider routing and automatic fallback chains
- Conversation memory with MessageWindowChatMemory
- RAG pipeline — PDF ingestion, vector embeddings, similarity search
- Chroma vector database integration via Docker
- Redis caching with configurable TTL
- PostgreSQL integration with Spring Data JPA
- Implementing Spring AI's ChatMemoryRepository for custom persistent storage
- Server-Sent Events (SSE) for real-time streaming responses
- Rate limiting with Bucket4j
- DTO pattern for clean API design
- Global exception handling with @RestControllerAdvice
- Serving a frontend from Spring Boot static resources
- Git branching, PR workflow, and GitHub Actions CI
- Docker Compose for local service orchestration

---

## 👨‍💻 Author

**Raghav Sharma**
- GitHub: [@Raghav-Sharma03](https://github.com/Raghav-Sharma03)

---

## 📄 License

This project is open source.