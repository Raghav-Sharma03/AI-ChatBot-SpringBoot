package project.chatbot.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Chat_Controller{

    private final ChatClient chatClient; // Object that acctually talk with the AI/Groq

    public Chat_Controller(ChatClient.Builder builder, ChatMemory chatMemory){      // configure ChatClient
        this.chatClient = builder                            // Create a ChatClient object using the builder
            .defaultSystem(" You are a helpful assistant")   // AI's permanent instruction or permanent system message
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()) // Add the chat memory advisor to the ChatClient
            .build();                                        // Creates the actual ChatClient object
    }


    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request,  @RequestParam(defaultValue = "default-session") String sessionId){
        return chatClient
            .prompt()
            .user(request.getMessage())    // Attach the user message to the prompt
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId)) // Attach the session ID to the prompt
            .call()           //Actually send the request to the AI model
            .content();       // Return the content of the response from the AI model
    }
}