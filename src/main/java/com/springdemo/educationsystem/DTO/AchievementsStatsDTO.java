package com.springdemo.educationsystem.DTO;

import lombok.Data;
import java.util.Map;

@Data
public class AchievementsStatsDTO {
    private int total;
    private int unlocked;
    private Map<String, Integer> unlockedByType;
    private Map<String, Integer> totalByType;
}
