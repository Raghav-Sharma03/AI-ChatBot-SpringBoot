package project.chatbot.controller;

import java.time.LocalDateTime;

public class SessionSummary {

    private String sessionId;
    private String title;
    private LocalDateTime lastTime;

    public SessionSummary(String sessionId, String title, LocalDateTime lastTime) {
        this.sessionId = sessionId;
        this.title = title;
        this.lastTime = lastTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getLastTime() {
        return lastTime;
    }
}