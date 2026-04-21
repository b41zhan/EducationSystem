package com.springdemo.educationsystem.DTO;

public class AdminSchoolClassCreateDTO {

    private Long schoolId;
    private String name;
    private String academicYear;
    private Long homeroomTeacherId; // nullable

    public AdminSchoolClassCreateDTO() {
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
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

    public Long getHomeroomTeacherId() {
        return homeroomTeacherId;
    }

    public void setHomeroomTeacherId(Long homeroomTeacherId) {
        this.homeroomTeacherId = homeroomTeacherId;
    }
}