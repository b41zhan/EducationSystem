package com.springdemo.educationsystem.DTO;

import java.util.ArrayList;
import java.util.List;

public class GradeTextQuizAnswersDTO {

    private Long attemptId;
    private List<TextQuestionGradeDTO> grades = new ArrayList<>();

    public GradeTextQuizAnswersDTO() {
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public List<TextQuestionGradeDTO> getGrades() {
        return grades;
    }

    public void setGrades(List<TextQuestionGradeDTO> grades) {
        this.grades = grades;
    }

    public static class TextQuestionGradeDTO {
        private Long questionId;
        private Integer pointsAwarded;

        public TextQuestionGradeDTO() {
        }

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public Integer getPointsAwarded() {
            return pointsAwarded;
        }

        public void setPointsAwarded(Integer pointsAwarded) {
            this.pointsAwarded = pointsAwarded;
        }
    }
}