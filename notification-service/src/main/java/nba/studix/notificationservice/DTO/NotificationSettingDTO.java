package nba.studix.notificationservice.DTO;

import java.time.LocalDateTime;

public class NotificationSettingDTO {
    private Long userId;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean assignmentNotifications;
    private Boolean gradeNotifications;
    private Boolean systemNotifications;
    private LocalDateTime updatedAt;

    // Конструкторы
    public NotificationSettingDTO() {}

    // Геттеры и сеттеры
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Boolean getEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(Boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public Boolean getPushNotifications() { return pushNotifications; }
    public void setPushNotifications(Boolean pushNotifications) { this.pushNotifications = pushNotifications; }

    public Boolean getAssignmentNotifications() { return assignmentNotifications; }
    public void setAssignmentNotifications(Boolean assignmentNotifications) { this.assignmentNotifications = assignmentNotifications; }

    public Boolean getGradeNotifications() { return gradeNotifications; }
    public void setGradeNotifications(Boolean gradeNotifications) { this.gradeNotifications = gradeNotifications; }

    public Boolean getSystemNotifications() { return systemNotifications; }
    public void setSystemNotifications(Boolean systemNotifications) { this.systemNotifications = systemNotifications; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}