package nba.studix.notificationservice.Service;

import nba.studix.notificationservice.DTO.NotificationDTO;
import nba.studix.notificationservice.DTO.CreateNotificationDTO;
import nba.studix.notificationservice.DTO.NotificationSettingDTO;
import nba.studix.notificationservice.Entity.Notification;
import nba.studix.notificationservice.Entity.NotificationSetting;
import nba.studix.notificationservice.Repository.NotificationRepository;
import nba.studix.notificationservice.Repository.NotificationSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationSettingRepository notificationSettingRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationSettingRepository = notificationSettingRepository;
    }

    public NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setIsRead(notification.getIsRead());
        dto.setRelatedId(notification.getRelatedId());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
        logger.info("Marked notification as read: {}", notificationId);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications =
                notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
        logger.info("Marked all notifications as read for user: {}", userId);
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public Notification createNotification(CreateNotificationDTO createDTO) {
        // Проверяем настройки уведомлений пользователя
        NotificationSetting settings = getNotificationSettings(createDTO.getUserId());

        // Проверяем, разрешены ли уведомления данного типа
        if (!isNotificationTypeAllowed(settings, createDTO.getType())) {
            logger.info("Notification type {} is disabled for user {}", createDTO.getType(), createDTO.getUserId());
            return null;
        }

        Notification notification = new Notification();
        notification.setUserId(createDTO.getUserId());
        notification.setMessage(createDTO.getMessage());
        notification.setType(createDTO.getType());
        notification.setRelatedId(createDTO.getRelatedId());

        Notification savedNotification = notificationRepository.save(notification);
        logger.info("Created notification for user: {}, type: {}, message: {}",
                createDTO.getUserId(), createDTO.getType(), createDTO.getMessage());

        return savedNotification;
    }

    public void createNewAssignmentNotification(Long userId, String assignmentTitle, Long assignmentId) {
        String message = "Новое задание: " + assignmentTitle;
        CreateNotificationDTO createDTO = new CreateNotificationDTO();
        createDTO.setUserId(userId);
        createDTO.setMessage(message);
        createDTO.setType("new_assignment");
        createDTO.setRelatedId(assignmentId);

        createNotification(createDTO);
    }

    public void createGradeNotification(Long userId, String assignmentTitle, Integer grade, Long submissionId) {
        String message = "Ваша работа \"" + assignmentTitle + "\" оценена: " + grade + "/100";
        CreateNotificationDTO createDTO = new CreateNotificationDTO();
        createDTO.setUserId(userId);
        createDTO.setMessage(message);
        createDTO.setType("grade");
        createDTO.setRelatedId(submissionId);

        createNotification(createDTO);
    }

    public void createSystemNotification(Long userId, String message, Long relatedId) {
        CreateNotificationDTO createDTO = new CreateNotificationDTO();
        createDTO.setUserId(userId);
        createDTO.setMessage(message);
        createDTO.setType("system");
        createDTO.setRelatedId(relatedId);

        createNotification(createDTO);
    }

    public void createBulkNotifications(List<Long> userIds, String message, String type, Long relatedId) {
        for (Long userId : userIds) {
            CreateNotificationDTO createDTO = new CreateNotificationDTO();
            createDTO.setUserId(userId);
            createDTO.setMessage(message);
            createDTO.setType(type);
            createDTO.setRelatedId(relatedId);

            createNotification(createDTO);
        }
        logger.info("Created bulk notifications for {} users, type: {}", userIds.size(), type);
    }

    private NotificationSetting getNotificationSettings(Long userId) {
        return notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Создаем настройки по умолчанию если их нет
                    NotificationSetting defaultSettings = new NotificationSetting(userId);
                    return notificationSettingRepository.save(defaultSettings);
                });
    }

    private boolean isNotificationTypeAllowed(NotificationSetting settings, String type) {
        switch (type) {
            case "new_assignment":
                return settings.getAssignmentNotifications();
            case "grade":
                return settings.getGradeNotifications();
            case "system":
                return settings.getSystemNotifications();
            default:
                return true;
        }
    }

    public NotificationSettingDTO getNotificationSettingsDTO(Long userId) {
        NotificationSetting settings = getNotificationSettings(userId);
        return convertToSettingsDTO(settings);
    }

    public NotificationSettingDTO updateNotificationSettings(Long userId, NotificationSettingDTO settingsDTO) {
        NotificationSetting settings = getNotificationSettings(userId);

        if (settingsDTO.getEmailNotifications() != null) {
            settings.setEmailNotifications(settingsDTO.getEmailNotifications());
        }
        if (settingsDTO.getPushNotifications() != null) {
            settings.setPushNotifications(settingsDTO.getPushNotifications());
        }
        if (settingsDTO.getAssignmentNotifications() != null) {
            settings.setAssignmentNotifications(settingsDTO.getAssignmentNotifications());
        }
        if (settingsDTO.getGradeNotifications() != null) {
            settings.setGradeNotifications(settingsDTO.getGradeNotifications());
        }
        if (settingsDTO.getSystemNotifications() != null) {
            settings.setSystemNotifications(settingsDTO.getSystemNotifications());
        }

        NotificationSetting savedSettings = notificationSettingRepository.save(settings);
        logger.info("Updated notification settings for user: {}", userId);

        return convertToSettingsDTO(savedSettings);
    }

    private NotificationSettingDTO convertToSettingsDTO(NotificationSetting settings) {
        NotificationSettingDTO dto = new NotificationSettingDTO();
        dto.setUserId(settings.getUserId());
        dto.setEmailNotifications(settings.getEmailNotifications());
        dto.setPushNotifications(settings.getPushNotifications());
        dto.setAssignmentNotifications(settings.getAssignmentNotifications());
        dto.setGradeNotifications(settings.getGradeNotifications());
        dto.setSystemNotifications(settings.getSystemNotifications());
        dto.setUpdatedAt(settings.getUpdatedAt());
        return dto;
    }

    public void cleanupOldNotifications() {
        // TODO: Реализовать очистку старых уведомлений (например, старше 30 дней)
        logger.info("Cleanup old notifications - to be implemented");
    }
}