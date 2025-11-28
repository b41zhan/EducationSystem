package com.springdemo.educationsystem.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_achievements")
public class StudentAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "progress_value")
    private Integer progressValue = 0;

    public StudentAchievement() {
        this.unlockedAt = LocalDateTime.now();
    }

    public StudentAchievement(Student student, Achievement achievement) {
        this();
        this.student = student;
        this.achievement = achievement;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Achievement getAchievement() { return achievement; }
    public void setAchievement(Achievement achievement) { this.achievement = achievement; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
    public Integer getProgressValue() { return progressValue; }
    public void setProgressValue(Integer progressValue) { this.progressValue = progressValue; }
}