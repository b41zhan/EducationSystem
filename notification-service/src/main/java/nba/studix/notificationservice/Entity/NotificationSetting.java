package nba.studix.notificationservice.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_settings")
public class NotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Column(name = "push_notifications")
    private Boolean pushNotifications = true;

    @Column(name = "assignment_notifications")
    private Boolean assignmentNotifications = true;

    @Column(name = "grade_notifications")
    private Boolean gradeNotifications = true;

    @Column(name = "system_notifications")
    private Boolean systemNotifications = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public NotificationSetting() {
        this.updatedAt = LocalDateTime.now();
    }

    public NotificationSetting(Long userId) {
        this();
        this.userId = userId;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Boolean getEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getPushNotifications() { return pushNotifications; }
    public void setPushNotifications(Boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getAssignmentNotifications() { return assignmentNotifications; }
    public void setAssignmentNotifications(Boolean assignmentNotifications) {
        this.assignmentNotifications = assignmentNotifications;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getGradeNotifications() { return gradeNotifications; }
    public void setGradeNotifications(Boolean gradeNotifications) {
        this.gradeNotifications = gradeNotifications;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getSystemNotifications() { return systemNotifications; }
    public void setSystemNotifications(Boolean systemNotifications) {
        this.systemNotifications = systemNotifications;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}