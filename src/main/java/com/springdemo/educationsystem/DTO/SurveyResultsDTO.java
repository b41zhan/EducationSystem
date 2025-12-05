package com.springdemo.educationsystem.DTO;

import java.util.List;

public class SurveyResultsDTO {
    private Long id;
    private String title;
    private List<QuestionResultDTO> questions;

    public static class QuestionResultDTO {
        private Long id;
        private String text;
        private String type;
        private List<OptionResultDTO> options; // для MULTIPLE_CHOICE
        private List<String> textAnswers;      // для TEXT

        public static class OptionResultDTO {
            private Long id;
            private String text;
            private long count;

            public OptionResultDTO() {}

            public Long getId() { return id; }
            public void setId(Long id) { this.id = id; }

            public String getText() { return text; }
            public void setText(String text) { this.text = text; }

            public long getCount() { return count; }
            public void setCount(long count) { this.count = count; }
        }
        public QuestionResultDTO() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<OptionResultDTO> getOptions() { return options; }
        public void setOptions(List<OptionResultDTO> options) { this.options = options; }

        public List<String> getTextAnswers() { return textAnswers; }
        public void setTextAnswers(List<String> textAnswers) { this.textAnswers = textAnswers; }
    }
    public SurveyResultsDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<QuestionResultDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionResultDTO> questions) { this.questions = questions; }
}

