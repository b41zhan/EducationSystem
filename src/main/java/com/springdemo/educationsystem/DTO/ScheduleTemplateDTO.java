package com.springdemo.educationsystem.DTO;

import java.time.LocalDate;
import java.util.List;

public class ScheduleTemplateDTO {
    private Long id;
    private Long classId;
    private String className;
    private Integer quarter;
    private Integer weekNumber;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private List<ScheduleDayDTO> days;

    public ScheduleTemplateDTO() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }

    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public LocalDate getWeekEnd() { return weekEnd; }
    public void setWeekEnd(LocalDate weekEnd) { this.weekEnd = weekEnd; }

    public List<ScheduleDayDTO> getDays() { return days; }
    public void setDays(List<ScheduleDayDTO> days) { this.days = days; }
}