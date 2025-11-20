package com.springdemo.educationsystem.Entity;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule_days")
public class ScheduleDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ScheduleTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "is_holiday")
    private Boolean isHoliday = false;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL)
    private List<Lesson> lessons = new ArrayList<>();


    public ScheduleDay() {
    }

    public ScheduleDay(Long id, ScheduleTemplate template, DayOfWeek dayOfWeek, LocalDate date, Boolean isHoliday, List<Lesson> lessons) {
        this.id = id;
        this.template = template;
        this.dayOfWeek = dayOfWeek;
        this.date = date;
        this.isHoliday = isHoliday;
        this.lessons = lessons;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ScheduleTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ScheduleTemplate template) {
        this.template = template;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean getIsHoliday() {
        return isHoliday;
    }

    public void setIsHoliday(Boolean holiday) {
        isHoliday = holiday;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }
}
