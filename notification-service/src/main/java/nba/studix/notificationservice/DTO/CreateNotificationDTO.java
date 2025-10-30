package nba.studix.notificationservice.DTO;

public class CreateNotificationDTO {
    private Long userId;
    private String message;
    private String type;
    private Long relatedId;

    // Конструкторы
    public CreateNotificationDTO() {}

    // Геттеры и сеттеры
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
}