package com.springdemo.educationsystem.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public class LessonDTO {
    private Long id;
    private Long dayId;
    private Integer lessonNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private String classroom;
    private String className;
    private String dayOfWeek; // ДОБАВЛЯЕМ ПОЛЕ ДЛЯ ДНЯ НЕДЕЛИ
    private LocalDate date;

    public LessonDTO() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}