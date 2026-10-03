package project.chatbot.controller;

import java.time.LocalDateTime;

public class ChatResponse {

    private String message;
    private String sessionId;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;

    public ChatResponse(String message, String sessionId, LocalDateTime requestTime, LocalDateTime responseTime) {
        this.message = message;
        this.sessionId = sessionId;
        this.requestTime = requestTime;
        this.responseTime = responseTime;
    }

    public String getMessage() { return message; }
    public String getSessionId() { return sessionId; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public LocalDateTime getResponseTime() { return responseTime; }
}