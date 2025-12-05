package com.springdemo.educationsystem.Entity;
import jakarta.persistence.*;

@Entity
@Table(name = "survey_answers")
public class SurveyAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private SurveyResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private SurveyOption selectedOption; // для MULTIPLE_CHOICE

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer; // для TEXT

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SurveyResponse getResponse() { return response; }
    public void setResponse(SurveyResponse response) { this.response = response; }

    public SurveyQuestion getQuestion() { return question; }
    public void setQuestion(SurveyQuestion question) { this.question = question; }

    public SurveyOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(SurveyOption selectedOption) { this.selectedOption = selectedOption; }

    public String getTextAnswer() { return textAnswer; }
    public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }
}
