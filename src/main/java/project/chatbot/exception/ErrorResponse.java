package project.chatbot.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private int status;
    private String error;
    private String path;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String path) {
        this.status = status;
        this.error = error;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getPath() { return path; }
    public LocalDateTime getTimestamp() { return timestamp; }
}