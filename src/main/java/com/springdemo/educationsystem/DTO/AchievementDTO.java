package com.springdemo.educationsystem.DTO;

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

    public AchievementDTO() {}

    // Геттеры и сеттеры
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
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }
}