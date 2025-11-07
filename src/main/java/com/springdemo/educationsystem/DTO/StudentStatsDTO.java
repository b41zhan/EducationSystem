package com.springdemo.educationsystem.DTO;

import java.util.List;

public class StudentStatsDTO {
    private Long studentId;
    private String studentName;
    private String className;
    private List<Integer> grades;
    private Double averageGrade;
    private Integer completedAssignments;
    private Integer totalAssignments;

    public StudentStatsDTO() {}

    // Конструктор
    public StudentStatsDTO(Long studentId, String studentName, String className,
                           List<Integer> grades, Double averageGrade,
                           Integer completedAssignments, Integer totalAssignments) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.grades = grades;
        this.averageGrade = averageGrade;
        this.completedAssignments = completedAssignments;
        this.totalAssignments = totalAssignments;
    }

    // Геттеры и сеттеры
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public List<Integer> getGrades() { return grades; }
    public void setGrades(List<Integer> grades) { this.grades = grades; }

    public Double getAverageGrade() { return averageGrade; }
    public void setAverageGrade(Double averageGrade) { this.averageGrade = averageGrade; }

    public Integer getCompletedAssignments() { return completedAssignments; }
    public void setCompletedAssignments(Integer completedAssignments) { this.completedAssignments = completedAssignments; }

    public Integer getTotalAssignments() { return totalAssignments; }
    public void setTotalAssignments(Integer totalAssignments) { this.totalAssignments = totalAssignments; }
}