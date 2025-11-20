package com.springdemo.educationsystem.DTO;


// Дополнительный DTO для сообщения-ответа
public class ReplyMessageDTO {
    private Long id;
    private String senderName;
    private String content;

    public ReplyMessageDTO() {}

    public ReplyMessageDTO(Long id, String senderName, String content) {
        this.id = id;
        this.senderName = senderName;
        this.content = content;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

