package com.springdemo.educationsystem.DTO;

import java.time.LocalDateTime;

public class QuizAttemptResponseDTO {

    private Long attemptId;

    private String status;

    private LocalDateTime startTime;

    private Long remainingSeconds;

    public QuizAttemptResponseDTO() {}

    public Long getAttemptId() {
        return attemptId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setRemainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}