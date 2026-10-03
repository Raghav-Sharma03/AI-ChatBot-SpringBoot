package project.chatbot.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder, ChatMemory chatMemory) { // configure ChatClient
        this.chatClient = builder                                           // Create a ChatClient object using the builder
            .defaultSystem("You are a helpful assistant.")                   // AI's permanent instruction or permanent system message
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())// Add the chat memory advisor to the ChatClient
            .build();                                                            // Creates the actual ChatClient object
    }

    public String chat(String message, String sessionId) {

        return chatClient
            .prompt()
            .user(message)   // Attach the user message to the prompt
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))// Attach the session ID to the prompt
            .call() //Actually send the request to the AI model
            .content();  // Return the content of the response from the AI model
    }
}