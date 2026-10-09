package project.chatbot.service;

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

    public ChatService(
            @Qualifier("geminiClient") ChatClient geminiClient,
            @Qualifier("openRouterClient") ChatClient openRouterClient,
            @Qualifier("ollamaClient") ChatClient ollamaClient,
            ChatMemory chatMemory) {
        this.geminiClient = geminiClient;
        this.openRouterClient = openRouterClient;
        this.ollamaClient = ollamaClient;
        this.chatMemory = chatMemory;
    }

    public ChatResult chat(String message, String sessionId) {
        if (message.length() < SHORT_MESSAGE_THRESHOLD) {
            log.info("Routing to Gemini — short message ({} chars)", message.length());
            return tryGemini(message, sessionId);
        } else {
            log.info("Routing to OpenRouter — long message ({} chars)", message.length());
            return tryOpenRouter(message, sessionId);
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