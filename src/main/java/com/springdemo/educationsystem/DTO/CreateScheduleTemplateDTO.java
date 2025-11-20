package com.springdemo.educationsystem.DTO;

import java.time.LocalDate;
import java.util.List;

public class CreateScheduleTemplateDTO {
    private Long classId;
    private Integer quarter;
    private Integer weekNumber;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private List<CreateScheduleDayDTO> days;

    public CreateScheduleTemplateDTO() {}

    // Геттеры и сеттеры
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }

    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public LocalDate getWeekEnd() { return weekEnd; }
    public void setWeekEnd(LocalDate weekEnd) { this.weekEnd = weekEnd; }

    public List<CreateScheduleDayDTO> getDays() { return days; }
    public void setDays(List<CreateScheduleDayDTO> days) { this.days = days; }
}