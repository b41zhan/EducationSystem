package com.springdemo.educationsystem.Entity;
import jakarta.persistence.*;

@Entity
@Table(name = "survey_options")
public class SurveyOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;

    @Column(nullable = false, length = 255)
    private String text;

    @Column(name = "order_index")
    private Integer orderIndex;

    public SurveyOption() {}

    // ===== Геттеры и сеттеры =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SurveyQuestion getQuestion() { return question; }
    public void setQuestion(SurveyQuestion question) { this.question = question; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
