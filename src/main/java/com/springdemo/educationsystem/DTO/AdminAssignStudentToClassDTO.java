package com.springdemo.educationsystem.DTO;

public class AdminAssignStudentToClassDTO {

    private Long studentId;
    private Long classId;

    public AdminAssignStudentToClassDTO() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }
}