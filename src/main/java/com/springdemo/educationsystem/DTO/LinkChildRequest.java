package com.springdemo.educationsystem.DTO;

import jakarta.validation.constraints.NotNull;

public class LinkChildRequest {
    @NotNull
    private Long studentId;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
}