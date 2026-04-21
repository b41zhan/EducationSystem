package com.springdemo.educationsystem.DTO;

public class AdminParentChildLinkDTO {

    private Long parentId;
    private Long studentId;

    public AdminParentChildLinkDTO() {
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}