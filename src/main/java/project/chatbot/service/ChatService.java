package project.chatbot.service;

import org.springframework.ai.document.Document;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.time.Instant;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient geminiClient;
    private final ChatClient openRouterClient;
    private final ChatClient ollamaClient;
    private final ChatMemory chatMemory;
    private final RagService ragService;
    private volatile Instant geminiCooldownUntil = Instant.MIN;

    public ChatService(
            @Qualifier("geminiClient") ChatClient geminiClient,
            @Qualifier("openRouterClient") ChatClient openRouterClient,
            @Qualifier("ollamaClient") ChatClient ollamaClient,
            ChatMemory chatMemory,
            RagService ragService) {
        this.geminiClient = geminiClient;
        this.openRouterClient = openRouterClient;
        this.ollamaClient = ollamaClient;
        this.chatMemory = chatMemory;
        this.ragService = ragService;
    }

public ChatResult chat(String message, String sessionId, List<String> selectedFiles) {
    List<Document> docs = ragService.search(message, sessionId, selectedFiles);
        String augmentedMessage = message;

        if (!docs.isEmpty()) {
            String context = docs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n"));
            augmentedMessage = "Use the following context to answer the question.\n\n"
                    + "Context:\n" + context
                    + "\n\nQuestion: " + message;
            log.info("RAG context added — {} chunks found.", docs.size());
        } else {
            log.info("No RAG context found. Answering from model knowledge.");
        }

        return tryGemini(augmentedMessage, sessionId);
    }
    public Flux<String> streamChat(String message, String sessionId, List<String> selectedFiles) {
    List<Document> docs = ragService.search(message, sessionId, selectedFiles);
    String augmentedMessage = message;
    List<String> sourceFiles = ragService.getSourceFiles(docs);

    if (!docs.isEmpty()) {
        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
        augmentedMessage = "Use the following context to answer the question.\n\n"
                + "Context:\n" + context
                + "\n\nQuestion: " + message;
        log.info("RAG context added for streaming — {} chunks found.", docs.size());
    } else {
        log.info("No RAG context found for streaming.");
    }

    Flux<String> responseStream = streamFromGemini(augmentedMessage);

    if (!sourceFiles.isEmpty()) {
        String sourcesToken = "__SOURCES__:" + String.join(",", sourceFiles) + "\n";
        return Flux.concat(Flux.just(sourcesToken), responseStream);
    }

    return responseStream;
}

private Flux<String> streamFromGemini(String message) {
    if (Instant.now().isBefore(geminiCooldownUntil)) {
        log.info("Gemini in cooldown. Streaming from OpenRouter.");
        return streamFromOpenRouter(message);
    }
    try {
        log.info("Streaming from Gemini.");
        return geminiClient.prompt()
.system("""
You are a helpful assistant. Always respond in clean, well-structured markdown.
Rules:
- Put a blank line between every heading and the paragraph that follows it.
- Put each bullet point on its own separate line.
- Always use triple backtick fenced code blocks with the language name. Put the opening triple backtick on its own line. Put the closing triple backtick on its own line.
- Never put two sections on the same line.
- Never use --- as a separator.
- Never repeat the question in your response.
- Never put a heading and its content on the same line.
""")
        .user(message)
        .stream()
        .content()
                .onErrorResume(e -> {
                    log.warn("Gemini stream failed: {}. Falling back to OpenRouter.", e.getMessage());
                    geminiCooldownUntil = Instant.now().plusSeconds(300);
                    return streamFromOpenRouter(message);
                });
    } catch (Exception e) {
        log.warn("Gemini stream failed at setup: {}. Falling back to OpenRouter.", e.getMessage());
        geminiCooldownUntil = Instant.now().plusSeconds(300);
        return streamFromOpenRouter(message);
    }
}

private Flux<String> streamFromOpenRouter(String message) {
    try {
        log.info("Streaming from OpenRouter.");
        return openRouterClient.prompt()
.system("""
You are a helpful assistant. Always respond in clean, well-structured markdown.
Rules:
- Put a blank line between every heading and the paragraph that follows it.
- Put each bullet point on its own separate line.
- Always use triple backtick fenced code blocks with the language name. Put the opening triple backtick on its own line. Put the closing triple backtick on its own line.
- Never put two sections on the same line.
- Never use --- as a separator.
- Never repeat the question in your response.
- Never put a heading and its content on the same line.
""")
        .user(message)
        .stream()
        .content()
                .onErrorResume(e -> {
                    log.warn("OpenRouter stream failed: {}. Falling back to Ollama.", e.getMessage());
                    return streamFromOllama(message);
                });
    } catch (Exception e) {
        log.warn("OpenRouter stream failed at setup: {}. Falling back to Ollama.", e.getMessage());
        return streamFromOllama(message);
    }
}

private Flux<String> streamFromOllama(String message) {
    log.info("Streaming from Ollama.");
    return ollamaClient.prompt()
            .user(message)
            .stream()
            .content();
}
     public Flux<String> streamChatOllama(String message, String sessionId, List<String> selectedFiles) {
    List<Document> docs = ragService.search(message, sessionId, selectedFiles);
    String augmentedMessage = message;
    List<String> sourceFiles = ragService.getSourceFiles(docs);

    if (!docs.isEmpty()) {
        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
        augmentedMessage = "Use the following context to answer the question.\n\n"
                + "Context:\n" + context
                + "\n\nQuestion: " + message;
        log.info("RAG context added for Ollama streaming — {} chunks found.", docs.size());
    } else {
        log.info("No RAG context found for Ollama streaming.");
    }

    log.info("Streaming directly from Ollama.");
    Flux<String> responseStream = ollamaClient.prompt()
            .user(augmentedMessage)
            .stream()
            .content();

    if (!sourceFiles.isEmpty()) {
        String sourcesToken = "__SOURCES__:" + String.join(",", sourceFiles) + "\n";
        return Flux.concat(Flux.just(sourcesToken), responseStream);
    }

    return responseStream;
}
      public void deleteConversation(String sessionId) {
        chatMemory.clear(sessionId);
        log.info("Conversation deleted for session: {}", sessionId);
    }

    private ChatResult tryGemini(String message, String sessionId) {
        if (Instant.now().isBefore(geminiCooldownUntil)) {
            log.info("Gemini is in cooldown. Routing directly to OpenRouter.");
            return tryOpenRouter(message, sessionId);
        }
        try {
            log.info("Routing to Gemini.");
            ChatResponse response = geminiClient.prompt()
                    .user(message)
                    .advisors(a -> a
                            .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .chatResponse();
                        geminiCooldownUntil = Instant.MIN;
            return toChatResult(response);
        } catch (Exception e) {
            log.warn("Gemini failed: {}. Falling back to OpenRouter.", e.getMessage());
            geminiCooldownUntil = Instant.now().plusSeconds(300);
            return tryOpenRouter(message, sessionId);
        }
    }

    private ChatResult tryOpenRouter(String message, String sessionId) {
        try {
            log.info("Routing to OpenRouter.");
            ChatResponse response = openRouterClient.prompt()
                    .user(message)
                    .advisors(a -> a
                            .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .chatResponse();
            return toChatResult(response);
        } catch (Exception e) {
            log.warn("OpenRouter failed: {}. Falling back to Ollama.", e.getMessage());
            return tryOllama(message, sessionId);
        }
    }

    private ChatResult tryOllama(String message, String sessionId) {
        log.info("Routing to Ollama.");
        ChatResponse response = ollamaClient.prompt()
                .user(message)
                .advisors(a -> a
                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .chatResponse();
        return toChatResult(response);
    }

    private ChatResult toChatResult(ChatResponse response) {
        String text = response.getResult().getOutput().getText();
        long promptTokens = 0;
        long completionTokens = 0;
        long totalTokens = 0;
        try {
            var usage = response.getMetadata().getUsage();
            promptTokens = usage.getPromptTokens();
            completionTokens = usage.getCompletionTokens();
            totalTokens = usage.getTotalTokens();
            log.info("Tokens used — prompt: {}, completion: {}, total: {}",
                    promptTokens, completionTokens, totalTokens);
        } catch (Exception e) {
            log.warn("Could not read token usage.");
        }
        return new ChatResult(text, promptTokens, completionTokens, totalTokens);
    }
}