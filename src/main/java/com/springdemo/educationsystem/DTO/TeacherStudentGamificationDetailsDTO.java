package com.springdemo.educationsystem.DTO;

import lombok.Data;
import java.util.List;

@Data
public class TeacherStudentGamificationDetailsDTO {

    private Long studentId;
    private String studentName;
    private String className;
    // Основная статистика
    private Integer totalXp;
    private Integer level;
    private Integer completedAssignments;
    private Integer perfectAssignments;
    private Integer currentStreak;
    private Integer maxStreak;
    // Прогресс уровня
    private Integer nextLevelXp;
    private Integer currentLevelXp;
    // Достижения
    private List<AchievementDTO> achievements;          // полученные
    private List<AchievementDTO> availableAchievements; // заблокированные
    private Integer achievementsUnlocked;
    private Integer totalAchievements;
    // История активности
    private List<ActivityDTO> recentActivity;
}
