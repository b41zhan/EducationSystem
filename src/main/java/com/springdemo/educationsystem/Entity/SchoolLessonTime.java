package com.springdemo.educationsystem.Entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "school_lesson_times")
public class SchoolLessonTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "lesson_number")
    private Integer lessonNumber;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "break_duration")
    private Integer breakDuration; // в минутах


    public SchoolLessonTime() {}

    public SchoolLessonTime(Long id, School school, Integer lessonNumber, LocalTime startTime, LocalTime endTime, Integer breakDuration) {
        this.id = id;
        this.school = school;
        this.lessonNumber = lessonNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakDuration = breakDuration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public Integer getLessonNumber() {
        return lessonNumber;
    }

    public void setLessonNumber(Integer lessonNumber) {
        this.lessonNumber = lessonNumber;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(Integer breakDuration) {
        this.breakDuration = breakDuration;
    }
}