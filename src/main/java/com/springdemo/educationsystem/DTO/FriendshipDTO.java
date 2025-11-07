package com.springdemo.educationsystem.DTO;

import java.time.LocalDateTime;

public class FriendshipDTO {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private String requesterEmail;
    private Long addresseeId;
    private String addresseeName;
    private String addresseeEmail;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FriendshipDTO() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getRequesterEmail() { return requesterEmail; }
    public void setRequesterEmail(String requesterEmail) { this.requesterEmail = requesterEmail; }
    public Long getAddresseeId() { return addresseeId; }
    public void setAddresseeId(Long addresseeId) { this.addresseeId = addresseeId; }
    public String getAddresseeName() { return addresseeName; }
    public void setAddresseeName(String addresseeName) { this.addresseeName = addresseeName; }
    public String getAddresseeEmail() { return addresseeEmail; }
    public void setAddresseeEmail(String addresseeEmail) { this.addresseeEmail = addresseeEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}