package com.springdemo.educationsystem.DTO;

import java.time.LocalTime;
import java.util.List;

public class CreateTeacherScheduleDTO {
    private Long teacherId;
    private Long dayId;
    private Integer lessonNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long subjectId;
    private Long classId;
    private String classroom;

    public CreateTeacherScheduleDTO() {}

    // Геттеры и сеттеры
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getDayId() { return dayId; }
    public void setDayId(Long dayId) { this.dayId = dayId; }
    public Integer getLessonNumber() { return lessonNumber; }
    public void setLessonNumber(Integer lessonNumber) { this.lessonNumber = lessonNumber; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
}