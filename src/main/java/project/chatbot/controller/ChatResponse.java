package project.chatbot.controller;

import java.time.LocalDateTime;

public class ChatResponse {

    private String message;
    private String sessionId;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;

    public ChatResponse(String message, String sessionId, LocalDateTime requestTime,
                        LocalDateTime responseTime, long promptTokens,
                        long completionTokens, long totalTokens) {
        this.message = message;
        this.sessionId = sessionId;
        this.requestTime = requestTime;
        this.responseTime = responseTime;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    public String getMessage() { return message; }
    public String getSessionId() { return sessionId; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public LocalDateTime getResponseTime() { return responseTime; }
    public long getPromptTokens() { return promptTokens; }
    public long getCompletionTokens() { return completionTokens; }
    public long getTotalTokens() { return totalTokens; }
}