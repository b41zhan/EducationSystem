package com.springdemo.educationsystem.DTO;

public class UpsertFinalGradeDTO {
    private Long classId;
    private Long subjectId;
    private Long studentId;
    private Integer quarter;
    private Double quarterGrade;
    private Double yearGrade;

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public Double getQuarterGrade() { return quarterGrade; }
    public void setQuarterGrade(Double quarterGrade) { this.quarterGrade = quarterGrade; }

    public Double getYearGrade() { return yearGrade; }
    public void setYearGrade(Double yearGrade) { this.yearGrade = yearGrade; }
}