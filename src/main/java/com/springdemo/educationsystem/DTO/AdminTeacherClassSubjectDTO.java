package com.springdemo.educationsystem.DTO;

import java.time.LocalDateTime;

public class AdminTeacherClassSubjectDTO {

    private Long id;
    private boolean active;
    private LocalDateTime assignedAt;

    private Long teacherId;
    private Long teacherUserId;
    private String teacherFullName;
    private String teacherEmail;

    private Long classId;
    private String className;
    private String academicYear;

    private Long subjectId;
    private String subjectName;

    private Long schoolId;
    private String schoolName;

    public AdminTeacherClassSubjectDTO() {
    }

    public Long getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public Long getTeacherUserId() {
        return teacherUserId;
    }

    public String getTeacherFullName() {
        return teacherFullName;
    }

    public String getTeacherEmail() {
        return teacherEmail;
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

    public Long getSchoolId() {
        return schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public void setTeacherUserId(Long teacherUserId) {
        this.teacherUserId = teacherUserId;
    }

    public void setTeacherFullName(String teacherFullName) {
        this.teacherFullName = teacherFullName;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
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

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
}