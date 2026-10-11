package project.chatbot.service;

import org.springframework.ai.document.Document;
import project.chatbot.service.RagService;
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

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final int SHORT_MESSAGE_THRESHOLD = 100;

    private final ChatClient geminiClient;
    private final ChatClient openRouterClient;
    private final ChatClient ollamaClient;
    private final ChatMemory chatMemory;
    private final RagService ragService;

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

public ChatResult chat(String message, String sessionId) {
    List<Document> docs = ragService.search(message);
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

    if (augmentedMessage.length() < SHORT_MESSAGE_THRESHOLD) {
        log.info("Routing to Gemini — short message ({} chars)", message.length());
        return tryGemini(augmentedMessage, sessionId);
    } else {
        log.info("Routing to OpenRouter — long message ({} chars)", message.length());
        return tryOpenRouter(augmentedMessage, sessionId);
    }
}

    private ChatResult tryGemini(String message, String sessionId) {
        try {
            ChatResponse response = geminiClient.prompt()
                    .user(message)
                    .advisors(a -> a
                            .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .chatResponse();
            return toChatResult(response);
        } catch (Exception e) {
            log.warn("Gemini failed: {}. Falling back to Ollama.", e.getMessage(), e);
            return tryOllama(message, sessionId);
        }
    }

    private ChatResult tryOpenRouter(String message, String sessionId) {
        try {
            ChatResponse response = openRouterClient.prompt()
                    .user(message)
                    .advisors(a -> a
                            .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .chatResponse();
            return toChatResult(response);
        } catch (Exception e) {
            log.warn("OpenRouter failed: {}. Falling back to Ollama.", e.getMessage(), e);
            return tryOllama(message, sessionId);
        }
    }

    private ChatResult tryOllama(String message, String sessionId) {
        log.info("Using Ollama fallback.");
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