package com.springdemo.educationsystem.DTO;

public class LeaderboardDTO {
    private Long studentId;
    private String studentName;
    private String className;
    private Integer totalXp;
    private Integer level;
    private Integer rank;
    private Integer achievementsCount;
    private String profilePhotoPath;

    public LeaderboardDTO() {}

    // Геттеры и сеттеры
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Integer getTotalXp() { return totalXp; }
    public void setTotalXp(Integer totalXp) { this.totalXp = totalXp; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Integer getAchievementsCount() { return achievementsCount; }
    public void setAchievementsCount(Integer achievementsCount) { this.achievementsCount = achievementsCount; }
    public String getProfilePhotoPath() { return profilePhotoPath; }
    public void setProfilePhotoPath(String profilePhotoPath) { this.profilePhotoPath = profilePhotoPath; }
}