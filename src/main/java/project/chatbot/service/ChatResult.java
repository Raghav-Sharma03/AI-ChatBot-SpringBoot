package project.chatbot.service;

public class ChatResult {

    private final String text;
    private final long promptTokens;
    private final long completionTokens;
    private final long totalTokens;

    public ChatResult(String text, long promptTokens, long completionTokens, long totalTokens) {
        this.text = text;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    public String getText() { return text; }
    public long getPromptTokens() { return promptTokens; }
    public long getCompletionTokens() { return completionTokens; }
    public long getTotalTokens() { return totalTokens; }
}