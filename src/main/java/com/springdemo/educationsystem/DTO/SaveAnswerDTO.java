package com.springdemo.educationsystem.DTO;

public class SaveAnswerDTO {

    private Long attemptId;
    private Long questionId;

    private String answerText;
    private String selectedOptionIdsJson;
    private String matchingJson;
    private String orderingJson;

    public SaveAnswerDTO() {}

    public Long getAttemptId() {
        return attemptId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public String getSelectedOptionIdsJson() {
        return selectedOptionIdsJson;
    }

    public String getMatchingJson() {
        return matchingJson;
    }

    public String getOrderingJson() {
        return orderingJson;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public void setSelectedOptionIdsJson(String selectedOptionIdsJson) {
        this.selectedOptionIdsJson = selectedOptionIdsJson;
    }

    public void setMatchingJson(String matchingJson) {
        this.matchingJson = matchingJson;
    }

    public void setOrderingJson(String orderingJson) {
        this.orderingJson = orderingJson;
    }
}