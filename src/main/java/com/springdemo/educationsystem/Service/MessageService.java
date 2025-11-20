package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.ConversationDTO;
import com.springdemo.educationsystem.DTO.MessageDTO;
import com.springdemo.educationsystem.Entity.Message;
import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Repository.MessageRepository;
import com.springdemo.educationsystem.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public MessageDTO sendMessage(Long senderId, Long receiverId, String content) {
        System.out.println("Sending message from " + senderId + " to " + receiverId + ": " + content);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        System.out.println("Sender: " + sender.getEmail());
        System.out.println("Receiver: " + receiver.getEmail());

        Message message = new Message(sender, receiver, content);
        Message savedMessage = messageRepository.save(message);

        System.out.println("Message saved with ID: " + savedMessage.getId());

        return convertToDTO(savedMessage);
    }

    public List<MessageDTO> getConversation(Long user1Id, Long user2Id) {
        List<Message> messages = messageRepository.findConversation(user1Id, user2Id);

        // Помечаем сообщения как прочитанные
        List<Message> unreadMessages = messageRepository.findUnreadMessages(user2Id, user1Id);
        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(msg -> msg.setRead(true));
            messageRepository.saveAll(unreadMessages);
        }

        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    public List<ConversationDTO> getRecentConversations(Long userId) {
        List<Message> recentMessages = messageRepository.findRecentConversations(userId);

        return recentMessages.stream()
                .map(message -> {
                    ConversationDTO dto = new ConversationDTO();
                    dto.setConversationId(message.getConversationId());

                    // Определяем собеседника
                    User otherUser = message.getSender().getId().equals(userId) ?
                            message.getReceiver() : message.getSender();

                    dto.setOtherUserId(otherUser.getId());
                    dto.setOtherUserName(otherUser.getFirstName() + " " + otherUser.getLastName());
                    dto.setOtherUserAvatar(otherUser.getProfilePhotoPath());
                    dto.setLastMessage(message.getContent());
                    dto.setLastMessageTime(message.getCreatedAt());
                    dto.setRead(message.isRead());

                    // Подсчет непрочитанных сообщений
                    Long unreadCount = messageRepository.findUnreadMessages(otherUser.getId(), userId)
                            .stream().count();
                    dto.setUnreadCount(unreadCount.intValue());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
        dto.setSenderAvatar(message.getSender().getProfilePhotoPath());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setReceiverName(message.getReceiver().getFirstName() + " " + message.getReceiver().getLastName());
        dto.setContent(message.getContent());
        dto.setRead(message.isRead());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setConversationId(message.getConversationId());
        return dto;
    }


}