package com.springdemo.educationsystem.DTO;

public class AdminTeacherClassSubjectCreateDTO {

    private Long teacherId;
    private Long classId;
    private Long subjectId;

    public AdminTeacherClassSubjectCreateDTO() {
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public Long getClassId() {
        return classId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
}