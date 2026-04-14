package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.NotificationDTO;
import com.springdemo.educationsystem.Entity.Notification;
import com.springdemo.educationsystem.Entity.Submission;
import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Repository.NotificationRepository;
import com.springdemo.educationsystem.Repository.SubmissionRepository;
import com.springdemo.educationsystem.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository; // Добавили репозиторий

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               SubmissionRepository submissionRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setRelatedId(notification.getRelatedId());
        dto.setCreatedAt(notification.getCreatedAt());

        // Проверяем статус связанного объекта
        if (notification.getType().equals("submission_graded") && notification.getRelatedId() != null) {
            submissionRepository.findById(notification.getRelatedId()).ifPresent(submission -> {
                dto.setRelatedEntityStatus(submission.getStatus());
            });
        }

        return dto;
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdAndHiddenFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ... остальные методы остаются без изменений ...

    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseAndHiddenFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void hideNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setHidden(true);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseAndHiddenFalseOrderByCreatedAtDesc(userId);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndHiddenFalse(userId);
    }

    public void createNewAssignmentNotification(User user, String assignmentTitle, Long assignmentId) {
        String message = "Новое задание: " + assignmentTitle;
        Notification notification = new Notification(user, message, "new_assignment", assignmentId);
        notificationRepository.save(notification);
    }

    public void createFriendRequestNotification(User user, String requesterName, Long requesterId) {
        String message = String.format("%s отправил(а) вам запрос на дружбу", requesterName);
        Notification notification = new Notification(user, message, "friend_request", requesterId);
        notificationRepository.save(notification);
    }
}
