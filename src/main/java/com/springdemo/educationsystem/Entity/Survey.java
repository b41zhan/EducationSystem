package com.springdemo.educationsystem.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "surveys")
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "for_students")
    private boolean forStudents;

    @Column(name = "for_teachers")
    private boolean forTeachers;

    @Column(nullable = false) // можно и без этого, но так честнее
    private boolean active = true;


    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyQuestion> questions = new ArrayList<>();

    // ОТВЕТЫ (нужно для survey.getResponses() в сервисе)
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyResponse> responses = new ArrayList<>();

    // getters/setters
    public Survey() {}

    // ===== Геттеры и сеттеры =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isForStudents() { return forStudents; }
    public void setForStudents(boolean forStudents) { this.forStudents = forStudents; }

    public boolean isForTeachers() { return forTeachers; }
    public void setForTeachers(boolean forTeachers) { this.forTeachers = forTeachers; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public List<SurveyQuestion> getQuestions() { return questions; }
    public void setQuestions(List<SurveyQuestion> questions) { this.questions = questions; }

    public List<SurveyResponse> getResponses() { return responses; }
    public void setResponses(List<SurveyResponse> responses) { this.responses = responses; }
}
