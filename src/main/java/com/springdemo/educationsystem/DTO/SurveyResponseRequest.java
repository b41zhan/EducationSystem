package com.springdemo.educationsystem.DTO;

import java.util.List;

public class SurveyResponseRequest {
    private Long surveyId;
    private List<AnswerDTO> answers;

    public static class AnswerDTO {
        private Long questionId;
        private Long optionId;    // для MULTIPLE_CHOICE
        private String textAnswer; // для TEXT
        public AnswerDTO() {}

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public Long getOptionId() { return optionId; }
        public void setOptionId(Long optionId) { this.optionId = optionId; }

        public String getTextAnswer() { return textAnswer; }
        public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }
    }
    public SurveyResponseRequest() {}

    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }

    public List<AnswerDTO> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDTO> answers) { this.answers = answers; }
}

