package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.FriendshipDTO;
import com.springdemo.educationsystem.DTO.UserSearchDTO;
import com.springdemo.educationsystem.Service.FriendshipService;
import com.springdemo.educationsystem.Service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin("*")
public class FriendshipController {

    private static final Logger logger = LoggerFactory.getLogger(FriendshipController.class);

    private final FriendshipService friendshipService;
    private final AuthService authService;

    public FriendshipController(FriendshipService friendshipService, AuthService authService) {
        this.friendshipService = friendshipService;
        this.authService = authService;
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<?> sendFriendRequest(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            FriendshipDTO result = friendshipService.sendFriendRequest(currentUserId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error sending friend request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<?> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            FriendshipDTO result = friendshipService.acceptFriendRequest(friendshipId, currentUserId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error accepting friend request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reject/{friendshipId}")
    public ResponseEntity<?> rejectFriendRequest(
            @PathVariable Long friendshipId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            FriendshipDTO result = friendshipService.rejectFriendRequest(friendshipId, currentUserId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error rejecting friend request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{friendId}")
    public ResponseEntity<?> removeFriend(
            @PathVariable Long friendId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            friendshipService.removeFriend(currentUserId, friendId);
            return ResponseEntity.ok(Map.of("message", "Friend removed successfully"));
        } catch (Exception e) {
            logger.error("Error removing friend: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            List<FriendshipDTO> requests = friendshipService.getPendingRequests(currentUserId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            logger.error("Error getting pending requests: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search-users")
    public ResponseEntity<?> searchUsersForChat(
            @RequestParam String query,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            List<UserSearchDTO> users = friendshipService.searchUsers(query, currentUserId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error searching users for chat: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getFriends(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            List<UserSearchDTO> friends = friendshipService.getFriends(currentUserId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            logger.error("Error getting friends: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam String query,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            List<UserSearchDTO> users = friendshipService.searchUsers(query, currentUserId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error searching users: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getFriendshipStats(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long currentUserId = authService.getUserId(token);

        try {
            FriendshipService.FriendshipStatsDTO stats = friendshipService.getFriendshipStats(currentUserId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting friendship stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
}