package project.chatbot;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.setup.OpenAiSetup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class AppConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openRouterApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String openRouterBaseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String openRouterModel;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();
    }

    @Bean
    @Qualifier("geminiClient")
    public ChatClient geminiClient(GoogleGenAiChatModel model) {
        return ChatClient.builder(model)
                .defaultSystem("You are a helpful assistant.")
                .build();
    }

@Bean
@Qualifier("openRouterClient")
public ChatClient openRouterClient() {
    com.openai.client.OpenAIClient syncClient = OpenAiSetup.setupSyncClient(
            openRouterBaseUrl,
            openRouterApiKey,
            null, null, null, null,
            false, false,
            null,
            Duration.ofSeconds(120),
            0, null, null,
            ObservationRegistry.NOOP,
            null,
            List.of());

    com.openai.client.OpenAIClientAsync asyncClient = OpenAiSetup.setupAsyncClient(
            openRouterBaseUrl,
            openRouterApiKey,
            null, null, null, null,
            false, false,
            null,
            Duration.ofSeconds(120),
            0, null, null,
            ObservationRegistry.NOOP,
            null,
            List.of());

    OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .openAiClient(syncClient)
            .openAiClientAsync(asyncClient)
            .options(OpenAiChatOptions.builder()
                    .model(openRouterModel)
                    .build())
            .build();

    return ChatClient.builder(chatModel)
            .defaultSystem("You are a helpful assistant.")
            .build();
}

    @Bean
    @Qualifier("ollamaClient")
    public ChatClient ollamaClient(OllamaChatModel model) {
        return ChatClient.builder(model)
                .defaultSystem("You are a helpful assistant.")
                .build();
    }
}