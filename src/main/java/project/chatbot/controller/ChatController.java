package project.chatbot.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import project.chatbot.service.ChatResult;
import project.chatbot.service.ChatService;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request,
                             @RequestParam(defaultValue = "default-session") String sessionId) {
        LocalDateTime requestTime = LocalDateTime.now();
        ChatResult result = chatService.chat(request.getMessage(), sessionId);
        LocalDateTime responseTime = LocalDateTime.now();

        return new ChatResponse(
                result.getText(),
                sessionId,
                requestTime,
                responseTime,
                result.getPromptTokens(),
                result.getCompletionTokens(),
                result.getTotalTokens()
        );
    }
@PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamChat(@Valid @RequestBody ChatRequest request,
                             @RequestParam(defaultValue = "default-session") String sessionId) {
    SseEmitter emitter = new SseEmitter(60_000L);

    chatService.streamChat(request.getMessage(), sessionId)
            .subscribe(
                    token -> {
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    },
                    emitter::completeWithError,
                    emitter::complete
            );

    return emitter;
}
@PostMapping(value = "/chat/stream/ollama", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamChatOllama(@Valid @RequestBody ChatRequest request,
                                   @RequestParam(defaultValue = "default-session") String sessionId) {
    SseEmitter emitter = new SseEmitter(60_000L);

    chatService.streamChatOllama(request.getMessage(), sessionId)
            .subscribe(
                    token -> {
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    },
                    emitter::completeWithError,
                    emitter::complete
            );

    return emitter;
}
}