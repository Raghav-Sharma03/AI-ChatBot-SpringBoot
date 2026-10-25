package project.chatbot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "custom_title")
    private String customTitle;

    public ChatMessageEntity() {}

    public ChatMessageEntity(String conversationId, String role, String content, LocalDateTime timestamp) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }
}