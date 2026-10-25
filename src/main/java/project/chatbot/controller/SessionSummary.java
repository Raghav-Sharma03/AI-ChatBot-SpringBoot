package project.chatbot.controller;

import java.time.LocalDateTime;

public class SessionSummary {

    private String sessionId;
    private String title;
    private LocalDateTime lastTime;

    private String customTitle;

    public SessionSummary(String sessionId, String title, LocalDateTime lastTime, String customTitle) {
        this.sessionId = sessionId;
        this.title = title;
        this.lastTime = lastTime;
        this.customTitle = customTitle;
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

    public String getCustomTitle() {
        return customTitle;
    }
}