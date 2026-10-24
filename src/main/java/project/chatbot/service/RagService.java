package project.chatbot.service;

import org.springframework.ai.vectorstore.SearchRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;

    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

public void ingest(Resource pdfResource, String sessionId) {
    log.info("Starting PDF ingestion: {} for session: {}", pdfResource.getFilename(), sessionId);

    PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource);
    List<Document> documents = reader.read();

    TokenTextSplitter splitter = new TokenTextSplitter();
    List<Document> chunks = splitter.apply(documents);

    // Tag each chunk with sessionId metadata
String fileName = pdfResource.getFilename();
List<Document> taggedChunks = chunks.stream()
        .map(doc -> {
            Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
            metadata.put("sessionId", sessionId);
            metadata.put("fileName", fileName);
            return new Document(doc.getId(), doc.getText(), metadata);
        })
        .collect(Collectors.toList());

    log.info("Split into {} chunks. Storing in Chroma...", taggedChunks.size());
    vectorStore.add(taggedChunks);
    log.info("Ingestion complete.");
}

public List<Document> search(String query, String sessionId, List<String> selectedFiles) {
    String filter;
    if (selectedFiles != null && !selectedFiles.isEmpty()) {
        String fileFilter = selectedFiles.stream()
                .map(f -> "fileName == '" + f + "'")
                .collect(Collectors.joining(" || "));
        filter = "sessionId == '" + sessionId + "' && (" + fileFilter + ")";
    } else {
        filter = "sessionId == '" + sessionId + "'";
    }

    SearchRequest request = SearchRequest.builder()
            .query(query)
            .topK(4)
            .filterExpression(filter)
            .build();
    return vectorStore.similaritySearch(request);
}
public void deleteDocument(String sessionId, String fileName) {
    log.info("Deleting document: {} for session: {}", fileName, sessionId);
    SearchRequest request = SearchRequest.builder()
            .query(" ")
            .topK(1000)
            .filterExpression("sessionId == '" + sessionId + "' && fileName == '" + fileName + "'")
            .build();
    List<Document> docs = vectorStore.similaritySearch(request);
    if (!docs.isEmpty()) {
        List<String> ids = docs.stream()
                .map(Document::getId)
                .collect(Collectors.toList());
        vectorStore.delete(ids);
        log.info("Deleted {} chunks for file: {} in session: {}", ids.size(), fileName, sessionId);
    } else {
        log.info("No chunks found for file: {} in session: {}", fileName, sessionId);
    }
}

public void clearDocuments(String sessionId) {
    log.info("Clearing documents for session: {}", sessionId);
    SearchRequest request = SearchRequest.builder()
            .query(" ")
            .topK(1000)
            .filterExpression("sessionId == '" + sessionId + "'")
            .build();
    List<Document> docs = vectorStore.similaritySearch(request);
    if (!docs.isEmpty()) {
        List<String> ids = docs.stream()
                .map(Document::getId)
                .collect(Collectors.toList());
        vectorStore.delete(ids);
        log.info("Deleted {} document chunks for session: {}", ids.size(), sessionId);
    } else {
        log.info("No documents found for session: {}", sessionId);
    }
}
}