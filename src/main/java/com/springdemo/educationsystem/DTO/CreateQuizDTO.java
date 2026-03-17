package com.springdemo.educationsystem.DTO;

public class CreateQuizDTO {

    private String title;
    private String description;
    private Long subjectId;

    public CreateQuizDTO() {}

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
}