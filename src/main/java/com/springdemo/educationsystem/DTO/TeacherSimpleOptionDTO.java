package com.springdemo.educationsystem.DTO;

public class TeacherSimpleOptionDTO {

    private Long teacherId;
    private Long userId;
    private String fullName;
    private String email;

    public TeacherSimpleOptionDTO() {
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}