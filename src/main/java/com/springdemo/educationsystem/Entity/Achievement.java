package com.springdemo.educationsystem.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "achievements")
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private String icon; // путь к иконке

    @Column(name = "achievement_type", nullable = false)
    private String type; // 'assignments', 'grades', 'streak', 'speed'

    @Column(name = "required_value")
    private Integer requiredValue; // требуемое значение для получения

    @Column(name = "xp_reward")
    private Integer xpReward = 0; // награда в опыте

    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL)
    private List<StudentAchievement> studentAchievements = new ArrayList<>();

    public Achievement() {}

    // Конструкторы, геттеры, сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getRequiredValue() { return requiredValue; }
    public void setRequiredValue(Integer requiredValue) { this.requiredValue = requiredValue; }
    public Integer getXpReward() { return xpReward; }
    public void setXpReward(Integer xpReward) { this.xpReward = xpReward; }
    public List<StudentAchievement> getStudentAchievements() { return studentAchievements; }
    public void setStudentAchievements(List<StudentAchievement> studentAchievements) { this.studentAchievements = studentAchievements; }
}