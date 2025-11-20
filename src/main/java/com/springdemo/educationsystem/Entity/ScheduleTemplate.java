package com.springdemo.educationsystem.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule_templates")
public class ScheduleTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(nullable = false)
    private Integer quarter; // 1, 2, 3, 4

    @Column(name = "week_number")
    private Integer weekNumber; // 1-9 (неделя в четверти)

    @Column(name = "week_start")
    private LocalDate weekStart;

    @Column(name = "week_end")
    private LocalDate weekEnd;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<ScheduleDay> days = new ArrayList<>();



    public ScheduleTemplate() {

    }

    public ScheduleTemplate(Long id, SchoolClass schoolClass, Integer quarter, Integer weekNumber, LocalDate weekStart, LocalDate weekEnd, List<ScheduleDay> days) {
        this.id = id;
        this.schoolClass = schoolClass;
        this.quarter = quarter;
        this.weekNumber = weekNumber;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.days = days;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SchoolClass getSchoolClass() {
        return schoolClass;
    }

    public void setSchoolClass(SchoolClass schoolClass) {
        this.schoolClass = schoolClass;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(LocalDate weekEnd) {
        this.weekEnd = weekEnd;
    }

    public List<ScheduleDay> getDays() {
        return days;
    }

    public void setDays(List<ScheduleDay> days) {
        this.days = days;
    }
}