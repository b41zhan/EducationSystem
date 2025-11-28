package com.springdemo.educationsystem.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_stats")
public class StudentStats {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "total_xp")
    private Integer totalXp = 0;

    @Column(name = "level")
    private Integer level = 1;

    @Column(name = "completed_assignments")
    private Integer completedAssignments = 0;

    @Column(name = "perfect_assignments")
    private Integer perfectAssignments = 0;

    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Column(name = "max_streak")
    private Integer maxStreak = 0;

    @Column(name = "rank_position")
    private Integer rankPosition;

    public StudentStats() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Integer getTotalXp() { return totalXp; }
    public void setTotalXp(Integer totalXp) { this.totalXp = totalXp; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getCompletedAssignments() { return completedAssignments; }
    public void setCompletedAssignments(Integer completedAssignments) { this.completedAssignments = completedAssignments; }
    public Integer getPerfectAssignments() { return perfectAssignments; }
    public void setPerfectAssignments(Integer perfectAssignments) { this.perfectAssignments = perfectAssignments; }
    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }
    public Integer getMaxStreak() { return maxStreak; }
    public void setMaxStreak(Integer maxStreak) { this.maxStreak = maxStreak; }
    public Integer getRankPosition() { return rankPosition; }
    public void setRankPosition(Integer rankPosition) { this.rankPosition = rankPosition; }
}