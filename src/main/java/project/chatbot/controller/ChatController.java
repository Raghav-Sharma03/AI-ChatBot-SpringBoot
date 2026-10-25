package project.chatbot.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import project.chatbot.entity.ChatMessageEntity;
import project.chatbot.repository.ChatMessageRepository;
import project.chatbot.service.ChatResult;
import project.chatbot.service.ChatService;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;

    public ChatController(ChatService chatService, ChatMessageRepository chatMessageRepository) {
        this.chatService = chatService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request,
                             @RequestParam(defaultValue = "default-session") String sessionId) {
        LocalDateTime requestTime = LocalDateTime.now();
        ChatResult result = chatService.chat(request.getMessage(), sessionId, request.getSelectedFiles());
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

    chatService.streamChat(request.getMessage(), sessionId, request.getSelectedFiles())
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

    chatService.streamChatOllama(request.getMessage(), sessionId, request.getSelectedFiles())
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

@PostMapping("/chat/save")
public ResponseEntity<Void> saveMessages(
        @RequestBody SaveMessageRequest request,
        @RequestParam String sessionId) {
    ChatMessageEntity userEntity = new ChatMessageEntity(
            sessionId, "user", request.getUserMessage(), java.time.LocalDateTime.now());
    ChatMessageEntity aiEntity = new ChatMessageEntity(
            sessionId, "assistant", request.getAiResponse(), java.time.LocalDateTime.now());
    chatMessageRepository.save(userEntity);
    chatMessageRepository.save(aiEntity);
    return ResponseEntity.ok().build();
}


@GetMapping("/chat/sessions")
public List<SessionSummary> getSessions() {
    List<String> sessionIds = chatMessageRepository.findDistinctConversationIds();
    return sessionIds.stream()
            .map(sessionId -> {
                List<ChatMessageEntity> messages = chatMessageRepository
                        .findByConversationIdOrderByTimestampAsc(sessionId);
                String title = messages.stream()
                        .filter(m -> m.getRole().equals("user"))
                        .findFirst()
                        .map(m -> m.getContent().length() > 40
                                ? m.getContent().substring(0, 40) + "..."
                                : m.getContent())
                        .orElse("New conversation");
                LocalDateTime lastTime = messages.isEmpty()
                        ? LocalDateTime.MIN
                        : messages.get(messages.size() - 1).getTimestamp();
                return new SessionSummary(sessionId, title, lastTime);
            })
            .sorted((a, b) -> b.getLastTime().compareTo(a.getLastTime()))
            .collect(Collectors.toList());
}

@GetMapping("/chat/{sessionId}/messages")
public List<ChatMessageEntity> getMessages(@PathVariable String sessionId) {
    return chatMessageRepository.findByConversationIdOrderByTimestampAsc(sessionId);
}

@DeleteMapping("/chat/{sessionId}")
public ResponseEntity<Void> deleteConversation(@PathVariable String sessionId) {
    chatService.deleteConversation(sessionId);
    return ResponseEntity.noContent().build();
}
}