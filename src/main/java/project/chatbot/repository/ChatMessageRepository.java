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

    @Query("SELECT c.customTitle FROM ChatMessageEntity c WHERE c.conversationId = :conversationId AND c.customTitle IS NOT NULL ORDER BY c.timestamp ASC")
    List<String> findCustomTitleByConversationId(String conversationId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE ChatMessageEntity c SET c.customTitle = :customTitle WHERE c.conversationId = :conversationId")
    void updateCustomTitle(String conversationId, String customTitle);
}