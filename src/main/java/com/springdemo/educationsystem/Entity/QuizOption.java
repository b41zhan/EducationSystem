package com.springdemo.educationsystem.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "quiz_option")
public class QuizOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "is_correct")
    private Boolean isCorrect = false;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @Column(name = "match_key", length = 255)
    private String matchKey;

    @Column(name = "match_value", length = 255)
    private String matchValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore
    private QuizQuestion question;

    public QuizOption() {
    }

    public Long getId() {
        return id;
    }

    public String getOptionText() {
        return optionText;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public String getMatchKey() {
        return matchKey;
    }

    public String getMatchValue() {
        return matchValue;
    }

    public QuizQuestion getQuestion() {
        return question;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
    }

    public void setMatchValue(String matchValue) {
        this.matchValue = matchValue;
    }

    public void setQuestion(QuizQuestion question) {
        this.question = question;
    }
}