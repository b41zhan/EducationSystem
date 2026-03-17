package com.springdemo.educationsystem.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "quiz_answer")
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "selected_option_ids_json", columnDefinition = "TEXT")
    private String selectedOptionIdsJson;

    @Column(name = "matching_json", columnDefinition = "TEXT")
    private String matchingJson;

    @Column(name = "ordering_json", columnDefinition = "TEXT")
    private String orderingJson;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "points_awarded")
    private Integer pointsAwarded = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    @JsonIgnore
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    public QuizAnswer() {
        this.pointsAwarded = 0;
    }

    public Long getId() {
        return id;
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

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Integer getPointsAwarded() {
        return pointsAwarded;
    }

    public QuizAttempt getAttempt() {
        return attempt;
    }

    public QuizQuestion getQuestion() {
        return question;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public void setPointsAwarded(Integer pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public void setAttempt(QuizAttempt attempt) {
        this.attempt = attempt;
    }

    public void setQuestion(QuizQuestion question) {
        this.question = question;
    }
}