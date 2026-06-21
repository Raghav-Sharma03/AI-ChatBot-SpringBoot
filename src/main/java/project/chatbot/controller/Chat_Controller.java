package project.chatbot.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Chat_Controller{

    private final ChatClient chatClient; // Object that acctually talk with the AI/Groq

    public Chat_Controller(ChatClient.Builder builder){      // configure ChatClient
        this.chatClient = builder                            // Create a ChatClient object using the builder
            .defaultSystem(" You are a helpful assistant")   // AI's permanent instruction or permanent system message
            .build();                                        // Creates the actual ChatClient object
    }


    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request){
        return chatClient
            .prompt()
            .user(request.getMessage())    // Attach the user message to the prompt
            .call()           //Actually send the request to the AI model
            .content();       // Return the content of the response from the AI model
    }
}