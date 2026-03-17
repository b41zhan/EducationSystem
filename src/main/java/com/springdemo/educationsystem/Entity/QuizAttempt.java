package com.springdemo.educationsystem.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springdemo.educationsystem.Enum.QuizAttemptStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempt")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuizAttemptStatus status = QuizAttemptStatus.NOT_STARTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_assignment_id", nullable = false)
    private QuizAssignment quizAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAnswer> answers = new ArrayList<>();

    public QuizAttempt() {
        this.status = QuizAttemptStatus.NOT_STARTED;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public Integer getScore() {
        return score;
    }

    public QuizAttemptStatus getStatus() {
        return status;
    }

    public QuizAssignment getQuizAssignment() {
        return quizAssignment;
    }

    public Student getStudent() {
        return student;
    }

    public List<QuizAnswer> getAnswers() {
        return answers;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setStatus(QuizAttemptStatus status) {
        this.status = status;
    }

    public void setQuizAssignment(QuizAssignment quizAssignment) {
        this.quizAssignment = quizAssignment;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setAnswers(List<QuizAnswer> answers) {
        this.answers = answers;
    }
}