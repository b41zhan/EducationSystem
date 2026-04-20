package com.springdemo.educationsystem.DTO;

public class AdminSchoolClassUpdateDTO {

    private String name;
    private String academicYear;
    private Boolean active;

    public AdminSchoolClassUpdateDTO() {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}