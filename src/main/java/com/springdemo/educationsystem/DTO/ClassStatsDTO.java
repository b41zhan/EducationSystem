package com.springdemo.educationsystem.DTO;

import java.util.List;

public class ClassStatsDTO {
    private Long classId;
    private String className;
    private Integer totalStudents;
    private Integer totalAssignments;
    private Double classAverageGrade;
    private List<StudentStatsDTO> students;

    public ClassStatsDTO() {}

    // Геттеры и сеттеры
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }

    public Integer getTotalAssignments() { return totalAssignments; }
    public void setTotalAssignments(Integer totalAssignments) { this.totalAssignments = totalAssignments; }

    public Double getClassAverageGrade() { return classAverageGrade; }
    public void setClassAverageGrade(Double classAverageGrade) { this.classAverageGrade = classAverageGrade; }

    public List<StudentStatsDTO> getStudents() { return students; }
    public void setStudents(List<StudentStatsDTO> students) { this.students = students; }
}