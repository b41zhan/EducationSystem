package com.springdemo.educationsystem.DTO;

public class AdminSchoolClassDTO {

    private Long id;
    private String name;
    private String academicYear;
    private boolean active;
    private Long schoolId;
    private String schoolName;
    private int studentsCount;

    // НОВОЕ: данные классного руководителя
    private Long homeroomTeacherId;
    private Long homeroomTeacherUserId;
    private String homeroomTeacherFullName;
    private String homeroomTeacherEmail;

    public AdminSchoolClassDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public int getStudentsCount() {
        return studentsCount;
    }

    public void setStudentsCount(int studentsCount) {
        this.studentsCount = studentsCount;
    }

    public Long getHomeroomTeacherId() {
        return homeroomTeacherId;
    }

    public void setHomeroomTeacherId(Long homeroomTeacherId) {
        this.homeroomTeacherId = homeroomTeacherId;
    }

    public Long getHomeroomTeacherUserId() {
        return homeroomTeacherUserId;
    }

    public void setHomeroomTeacherUserId(Long homeroomTeacherUserId) {
        this.homeroomTeacherUserId = homeroomTeacherUserId;
    }

    public String getHomeroomTeacherFullName() {
        return homeroomTeacherFullName;
    }

    public void setHomeroomTeacherFullName(String homeroomTeacherFullName) {
        this.homeroomTeacherFullName = homeroomTeacherFullName;
    }

    public String getHomeroomTeacherEmail() {
        return homeroomTeacherEmail;
    }

    public void setHomeroomTeacherEmail(String homeroomTeacherEmail) {
        this.homeroomTeacherEmail = homeroomTeacherEmail;
    }
}