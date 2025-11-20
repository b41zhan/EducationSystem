package com.springdemo.educationsystem.DTO;

import java.time.LocalTime;

public class CreateLessonDTO {
    private Integer lessonNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long subjectId;
    private Long teacherId;
    private String classroom;

    public CreateLessonDTO() {}

    // Геттеры и сеттеры
    public Integer getLessonNumber() { return lessonNumber; }
    public void setLessonNumber(Integer lessonNumber) { this.lessonNumber = lessonNumber; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
}