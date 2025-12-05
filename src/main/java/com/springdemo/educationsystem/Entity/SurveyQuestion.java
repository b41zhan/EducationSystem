package com.springdemo.educationsystem.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_questions")
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private SurveyQuestionType type; // MULTIPLE_CHOICE, TEXT

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "required", nullable = false)
    private boolean required = true; // или false, как тебе нужно по умолчанию

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyOption> options = new ArrayList<>();

    public SurveyQuestion() {}

    // ===== Геттеры и сеттеры =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public SurveyQuestionType getType() { return type; }
    public void setType(SurveyQuestionType type) { this.type = type; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }


    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public List<SurveyOption> getOptions() { return options; }
    public void setOptions(List<SurveyOption> options) { this.options = options; }
}
