package com.springdemo.educationsystem.DTO;

public class TeacherJournalClassDTO {
    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;

    public TeacherJournalClassDTO() {}

    public TeacherJournalClassDTO(Long classId, String className, Long subjectId, String subjectName) {
        this.classId = classId;
        this.className = className;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
}