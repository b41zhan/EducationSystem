package com.springdemo.educationsystem.DTO;

public class AdminTransferStudentDTO {

    private Long studentId;
    private Long toClassId;

    public AdminTransferStudentDTO() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getToClassId() {
        return toClassId;
    }

    public void setToClassId(Long toClassId) {
        this.toClassId = toClassId;
    }
}