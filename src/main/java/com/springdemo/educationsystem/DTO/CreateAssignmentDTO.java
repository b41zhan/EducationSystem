package com.springdemo.educationsystem.DTO;

import java.time.LocalDateTime;

public class CreateAssignmentDTO {
    private String title;
    private String description;
    private Integer maxGrade;
    private LocalDateTime deadline;
    private String type;
    private Long subjectId;
    private Long classId;

    // Геттеры и сеттеры
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxGrade() { return maxGrade; }
    public void setMaxGrade(Integer maxGrade) { this.maxGrade = maxGrade; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
}