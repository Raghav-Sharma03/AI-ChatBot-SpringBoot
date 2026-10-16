package project.chatbot.repository;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.chatbot.entity.ChatMessageEntity;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PostgresChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMessageRepository chatMessageRepository;

    public PostgresChatMemoryRepository(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return chatMessageRepository
                .findByConversationIdOrderByTimestampAsc(conversationId)
                .stream()
                .map(this::toMessage)
                .toList();
    }

    @Override
    @Transactional
    public void saveAll(String conversationId, List<Message> messages) {
        for (Message message : messages) {
            String role = message.getMessageType().getValue();
            String content = message.getText();
            ChatMessageEntity entity = new ChatMessageEntity(
                    conversationId, role, content, LocalDateTime.now()
            );
            chatMessageRepository.save(entity);
        }
    }

@Override
@Transactional
public void deleteByConversationId(String conversationId) {
    chatMessageRepository.deleteByConversationId(conversationId);
}

@Override
public List<String> findConversationIds() {
    return chatMessageRepository.findDistinctConversationIds();
}

    private Message toMessage(ChatMessageEntity entity) {
        if (entity.getRole().equals(MessageType.USER.getValue())) {
            return new UserMessage(entity.getContent());
        } else {
            return new AssistantMessage(entity.getContent());
        }
    }
}