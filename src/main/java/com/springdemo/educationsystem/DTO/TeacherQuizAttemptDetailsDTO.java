package com.springdemo.educationsystem.DTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TeacherQuizAttemptDetailsDTO {

    private Long attemptId;
    private Long assignmentId;
    private Long quizId;
    private String quizTitle;

    private Long studentId;
    private String studentName;

    private Integer score;
    private String status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationSeconds;

    private List<QuestionReviewDTO> questions = new ArrayList<>();

    public TeacherQuizAttemptDetailsDTO() {
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public List<QuestionReviewDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionReviewDTO> questions) {
        this.questions = questions;
    }

    public static class QuestionReviewDTO {
        private Long questionId;
        private String questionText;
        private String questionType;
        private Integer points;

        private String studentAnswerText;
        private String studentSelectedOptionIdsJson;

        private Boolean isCorrect;
        private Integer pointsAwarded;

        private boolean manuallyGradable;

        private List<OptionDTO> options = new ArrayList<>();

        public QuestionReviewDTO() {
        }

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public String getQuestionType() {
            return questionType;
        }

        public void setQuestionType(String questionType) {
            this.questionType = questionType;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }

        public String getStudentAnswerText() {
            return studentAnswerText;
        }

        public void setStudentAnswerText(String studentAnswerText) {
            this.studentAnswerText = studentAnswerText;
        }

        public String getStudentSelectedOptionIdsJson() {
            return studentSelectedOptionIdsJson;
        }

        public void setStudentSelectedOptionIdsJson(String studentSelectedOptionIdsJson) {
            this.studentSelectedOptionIdsJson = studentSelectedOptionIdsJson;
        }

        public Boolean getIsCorrect() {
            return isCorrect;
        }

        public void setIsCorrect(Boolean correct) {
            isCorrect = correct;
        }

        public Integer getPointsAwarded() {
            return pointsAwarded;
        }

        public void setPointsAwarded(Integer pointsAwarded) {
            this.pointsAwarded = pointsAwarded;
        }

        public boolean isManuallyGradable() {
            return manuallyGradable;
        }

        public void setManuallyGradable(boolean manuallyGradable) {
            this.manuallyGradable = manuallyGradable;
        }

        public List<OptionDTO> getOptions() {
            return options;
        }

        public void setOptions(List<OptionDTO> options) {
            this.options = options;
        }
    }

    public static class OptionDTO {
        private Long id;
        private String optionText;
        private Boolean correct;

        public OptionDTO() {
        }

        public OptionDTO(Long id, String optionText, Boolean correct) {
            this.id = id;
            this.optionText = optionText;
            this.correct = correct;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getOptionText() {
            return optionText;
        }

        public void setOptionText(String optionText) {
            this.optionText = optionText;
        }

        public Boolean getCorrect() {
            return correct;
        }

        public void setCorrect(Boolean correct) {
            this.correct = correct;
        }
    }
}