package project.chatbot;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    // This class can be used to configure application-wide settings, beans, or other configurations.
    // For example, you can define beans for ChatClient, ChatMemory, etc., if needed.
    

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .maxMessages(10)
            .build();
    }

}
