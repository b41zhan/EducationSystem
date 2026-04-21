package com.springdemo.educationsystem.DTO;

public class TeacherClassSubjectPairDTO {

    private Long assignmentId;
    private Long classId;
    private String className;
    private String academicYear;
    private Long subjectId;
    private String subjectName;

    public TeacherClassSubjectPairDTO() {
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public Long getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}