package com.springdemo.educationsystem.DTO;

import java.util.ArrayList;
import java.util.List;

public class AdminClassDetailsDTO {

    private AdminSchoolClassDTO schoolClass;
    private List<AdminStudentInClassDTO> students = new ArrayList<>();

    public AdminClassDetailsDTO() {
    }

    public AdminSchoolClassDTO getSchoolClass() {
        return schoolClass;
    }

    public void setSchoolClass(AdminSchoolClassDTO schoolClass) {
        this.schoolClass = schoolClass;
    }

    public List<AdminStudentInClassDTO> getStudents() {
        return students;
    }

    public void setStudents(List<AdminStudentInClassDTO> students) {
        this.students = students;
    }
}