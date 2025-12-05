package com.springdemo.educationsystem.DTO;

import java.util.List;

public class SurveyCreateDTO {
    private String title;
    private String description;
    private boolean forStudents;
    private boolean forTeachers;
    private List<QuestionDTO> questions;

    public static class QuestionDTO {
        private String text;
        private String type;
        private List<String> options;

        public QuestionDTO() {}

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
    }
    public SurveyCreateDTO() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isForStudents() { return forStudents; }
    public void setForStudents(boolean forStudents) { this.forStudents = forStudents; }

    public boolean isForTeachers() { return forTeachers; }
    public void setForTeachers(boolean forTeachers) { this.forTeachers = forTeachers; }

    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }
}
