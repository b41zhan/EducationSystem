package com.springdemo.educationsystem.DTO;

public class SurveyShortDTO {
    private Long id;
    private String title;
    private String description;
    private int questionsCount;

    public SurveyShortDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuestionsCount() { return questionsCount; }
    public void setQuestionsCount(int questionsCount) { this.questionsCount = questionsCount; }
}
