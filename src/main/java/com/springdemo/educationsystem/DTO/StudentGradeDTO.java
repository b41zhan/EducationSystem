package com.springdemo.educationsystem.DTO;

import java.time.LocalDateTime;


public class StudentGradeDTO {

    private String assignmentTitle;
    private String subjectName;
    private Integer grade;
    private LocalDateTime gradedAt;
    private String comment;

    public StudentGradeDTO() {
    }

    public StudentGradeDTO(String assignmentTitle,
                           String subjectName,
                           Integer grade,
                           LocalDateTime gradedAt,
                           String comment) {
        this.assignmentTitle = assignmentTitle;
        this.subjectName = subjectName;
        this.grade = grade;
        this.gradedAt = gradedAt;
        this.comment = comment;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
