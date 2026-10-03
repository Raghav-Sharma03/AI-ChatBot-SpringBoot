package project.chatbot.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import project.chatbot.service.ChatService;

@RestController
@RequestMapping("/api")
public class Chat_Controller{

    private final ChatService chatService;

    public Chat_Controller(ChatService chatService){
        this.chatService = chatService;
    }


    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request,  @RequestParam(defaultValue = "default-session") String sessionId){
        LocalDateTime requestTime = LocalDateTime.now();
        String message = chatService.chat(request.getMessage(), sessionId);
        LocalDateTime responseTime = LocalDateTime.now();

        return new ChatResponse(message, sessionId, requestTime, responseTime);
    }
}