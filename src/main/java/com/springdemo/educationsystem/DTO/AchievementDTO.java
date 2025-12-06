package com.springdemo.educationsystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AchievementDTO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String type;
    private Integer requiredValue;
    private Integer xpReward;
    private boolean unlocked;
    private Integer progress;
    private Integer progressPercentage;
    private LocalDateTime unlockedAt;

    public AchievementDTO() {}
}