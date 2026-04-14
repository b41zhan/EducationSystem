package com.springdemo.educationsystem.DTO;

public class UpsertAttendanceDTO {
    private Long classId;
    private Long subjectId;
    private Long studentId;
    private Integer quarter;
    private String lessonDate;
    private String status;

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public String getLessonDate() { return lessonDate; }
    public void setLessonDate(String lessonDate) { this.lessonDate = lessonDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}