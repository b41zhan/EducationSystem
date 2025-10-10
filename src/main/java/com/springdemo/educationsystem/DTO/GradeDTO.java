package com.springdemo.educationsystem.DTO;

import java.time.LocalDateTime;

public class GradeDTO {
    private Long submissionId;
    private Integer gradeValue;
    private String comment;

    public GradeDTO() {}

    // Геттеры и сеттеры
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}