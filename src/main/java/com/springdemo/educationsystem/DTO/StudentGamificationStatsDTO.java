package com.springdemo.educationsystem.DTO;

import java.util.List;

public class StudentGamificationStatsDTO {
    private Long studentId;
    private String studentName;
    private Integer totalXp;
    private Integer level;
    private Integer rank;
    private Integer completedAssignments;
    private Integer perfectAssignments;
    private Integer currentStreak;
    private Integer maxStreak;
    private Integer achievementsUnlocked;
    private Integer totalAchievements;
    private List<AchievementDTO> recentAchievements;
    private Integer nextLevelXp;
    private Integer currentLevelXp;

    public StudentGamificationStatsDTO() {}

    // Геттеры и сеттеры
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Integer getTotalXp() { return totalXp; }
    public void setTotalXp(Integer totalXp) { this.totalXp = totalXp; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Integer getCompletedAssignments() { return completedAssignments; }
    public void setCompletedAssignments(Integer completedAssignments) { this.completedAssignments = completedAssignments; }
    public Integer getPerfectAssignments() { return perfectAssignments; }
    public void setPerfectAssignments(Integer perfectAssignments) { this.perfectAssignments = perfectAssignments; }
    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }
    public Integer getMaxStreak() { return maxStreak; }
    public void setMaxStreak(Integer maxStreak) { this.maxStreak = maxStreak; }
    public Integer getAchievementsUnlocked() { return achievementsUnlocked; }
    public void setAchievementsUnlocked(Integer achievementsUnlocked) { this.achievementsUnlocked = achievementsUnlocked; }
    public Integer getTotalAchievements() { return totalAchievements; }
    public void setTotalAchievements(Integer totalAchievements) { this.totalAchievements = totalAchievements; }
    public List<AchievementDTO> getRecentAchievements() { return recentAchievements; }
    public void setRecentAchievements(List<AchievementDTO> recentAchievements) { this.recentAchievements = recentAchievements; }
    public Integer getNextLevelXp() { return nextLevelXp; }
    public void setNextLevelXp(Integer nextLevelXp) { this.nextLevelXp = nextLevelXp; }
    public Integer getCurrentLevelXp() { return currentLevelXp; }
    public void setCurrentLevelXp(Integer currentLevelXp) { this.currentLevelXp = currentLevelXp; }
}