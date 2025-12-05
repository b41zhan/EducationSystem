package com.springdemo.educationsystem.DTO;

import java.util.List;

public class SurveyDetailsDTO {
    private Long id;
    private String title;
    private String description;
    private List<QuestionDTO> questions;

    public static class QuestionDTO {
        private Long id;
        private String text;
        private String type; // MULTIPLE_CHOICE / TEXT
        private List<OptionDTO> options;

        public static class OptionDTO {
            private Long id;
            private String text;

            public OptionDTO() {}

            public Long getId() { return id; }
            public void setId(Long id) { this.id = id; }

            public String getText() { return text; }
            public void setText(String text) { this.text = text; }
        }
        // getters/setters

        public QuestionDTO() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<OptionDTO> getOptions() { return options; }
        public void setOptions(List<OptionDTO> options) { this.options = options; }
    }
    public SurveyDetailsDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }
}
