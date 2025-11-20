package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.ConversationDTO;
import com.springdemo.educationsystem.DTO.MessageDTO;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class MessageController {

    private final MessageService messageService;
    private final AuthService authService;

    public MessageController(MessageService messageService, AuthService authService) {
        this.messageService = messageService;
        this.authService = authService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestBody Map<String, Object> messageData,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long senderId = authService.getUserId(token);
        Long receiverId = Long.valueOf(messageData.get("receiverId").toString());
        String content = (String) messageData.get("content");

        try {
            MessageDTO message = messageService.sendMessage(senderId, receiverId, content);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<?> getConversation(
            @PathVariable Long otherUserId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            List<MessageDTO> messages = messageService.getConversation(currentUserId, otherUserId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getRecentConversations(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            List<ConversationDTO> conversations = messageService.getRecentConversations(currentUserId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            Long unreadCount = messageService.getUnreadCount(currentUserId);
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
}
