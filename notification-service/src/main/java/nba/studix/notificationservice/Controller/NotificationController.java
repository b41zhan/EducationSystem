package nba.studix.notificationservice.Controller;

import nba.studix.notificationservice.DTO.NotificationDTO;
import nba.studix.notificationservice.DTO.CreateNotificationDTO;
import nba.studix.notificationservice.DTO.NotificationSettingDTO;
import nba.studix.notificationservice.Service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getUserNotifications(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить userId из токена (временная заглушка)
            Long userId = 3L; // Заглушка для user ID

            List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            logger.error("Error getting user notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить userId из токена (временная заглушка)
            Long userId = 3L; // Заглушка для user ID

            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            logger.error("Error getting unread notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить userId из токена (временная заглушка)
            Long userId = 3L; // Заглушка для user ID

            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("count", count));

        } catch (Exception e) {
            logger.error("Error getting unread count: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{notificationId}/mark-read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId,
                                        @RequestHeader("Authorization") String authorizationHeader) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить userId из токена (временная заглушка)
            Long userId = 3L; // Заглушка для user ID

            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody CreateNotificationDTO createDTO) {
        try {
            notificationService.createNotification(createDTO);
            return ResponseEntity.ok(Map.of("message", "Notification created successfully"));
        } catch (Exception e) {
            logger.error("Error creating notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<?> getNotificationSettings(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить userId из токена (временная заглушка)
            Long userId = 3L; // Заглушка для user ID

            NotificationSettingDTO settings = notificationService.getNotificationSettingsDTO(userId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            logger.error("Error getting notification settings: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateNotificationSettings(@RequestBody NotificationSettingDTO settingsDTO,
                                                        @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить userId из токена (временная заглушка)
            Long userId = 3L; // Заглушка для user ID

            NotificationSettingDTO updatedSettings = notificationService.updateNotificationSettings(userId, settingsDTO);
            return ResponseEntity.ok(updatedSettings);
        } catch (Exception e) {
            logger.error("Error updating notification settings: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createBulkNotifications(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");
            String message = (String) request.get("message");
            String type = (String) request.get("type");
            Long relatedId = request.get("relatedId") != null ? Long.valueOf(request.get("relatedId").toString()) : null;

            notificationService.createBulkNotifications(userIds, message, type, relatedId);
            return ResponseEntity.ok(Map.of("message", "Bulk notifications created successfully"));
        } catch (Exception e) {
            logger.error("Error creating bulk notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}