package project.chatbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
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

    public String chat(String message, String sessionId) {
        if (message.length() < SHORT_MESSAGE_THRESHOLD) {
            log.info("Routing to Gemini — short message ({} chars)", message.length());
            return tryGemini(message, sessionId);
        } else {
            log.info("Routing to OpenRouter — long message ({} chars)", message.length());
            return tryOpenRouter(message, sessionId);
        }
    }

    private String tryGemini(String message, String sessionId) {
        try {
            return geminiClient.prompt()
                    .user(message)
                    .advisors(a -> a
                            .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("Gemini failed: {}. Falling back to Ollama.", e.getMessage(), e);
            return tryOllama(message, sessionId);
        }
    }

    private String tryOpenRouter(String message, String sessionId) {
        try {
            return openRouterClient.prompt()
                    .user(message)
                    .advisors(a -> a
                            .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("OpenRouter failed: {}. Falling back to Ollama.", e.getMessage());
            return tryOllama(message, sessionId);
        }
    }

    private String tryOllama(String message, String sessionId) {
        log.info("Using Ollama fallback.");
        return ollamaClient.prompt()
                .user(message)
                .advisors(a -> a
                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }
}