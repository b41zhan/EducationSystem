package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Получить все сообщения между двумя пользователями
    @Query("SELECT m FROM Message m WHERE " +
            "((m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
            "(m.sender.id = :user2Id AND m.receiver.id = :user1Id)) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    // Получить последние сообщения для каждого диалога пользователя
    @Query("SELECT m FROM Message m WHERE " +
            "m.id IN (SELECT MAX(m2.id) FROM Message m2 WHERE " +
            "m2.sender.id = :userId OR m2.receiver.id = :userId " +
            "GROUP BY m2.conversationId) " +
            "ORDER BY m.createdAt DESC")
    List<Message> findRecentConversations(@Param("userId") Long userId);

    // Получить количество непрочитанных сообщений
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
            "m.receiver.id = :userId AND m.isRead = false")
    Long countUnreadMessages(@Param("userId") Long userId);

    // Получить непрочитанные сообщения от конкретного пользователя
    @Query("SELECT m FROM Message m WHERE " +
            "m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    List<Message> findUnreadMessages(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    // Получить все диалоги пользователя
    @Query("SELECT DISTINCT m.conversationId FROM Message m WHERE " +
            "m.sender.id = :userId OR m.receiver.id = :userId")
    List<String> findUserConversations(@Param("userId") Long userId);
}