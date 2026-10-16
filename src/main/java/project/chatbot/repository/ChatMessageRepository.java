package project.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.chatbot.entity.ChatMessageEntity;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findByConversationIdOrderByTimestampAsc(String conversationId);

    void deleteByConversationId(String conversationId);

    @Query("SELECT DISTINCT c.conversationId FROM ChatMessageEntity c")
    List<String> findDistinctConversationIds();
}