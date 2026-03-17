package com.springdemo.educationsystem.DTO;

import com.springdemo.educationsystem.Enum.QuizQuestionType;
import java.util.List;

public class CreateQuestionDTO {

    private String questionText;
    private QuizQuestionType questionType;
    private Integer points;
    private Integer orderIndex;
    private List<CreateOptionDTO> options;

    public CreateQuestionDTO() {}

    public String getQuestionText() {
        return questionText;
    }

    public QuizQuestionType getQuestionType() {
        return questionType;
    }

    public Integer getPoints() {
        return points;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public List<CreateOptionDTO> getOptions() {
        return options;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setQuestionType(QuizQuestionType questionType) {
        this.questionType = questionType;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void setOptions(List<CreateOptionDTO> options) {
        this.options = options;
    }
}