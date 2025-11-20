package com.springdemo.educationsystem.DTO;

import java.time.LocalDate;
import java.util.List;

public class ScheduleDayDTO {
    private Long id;
    private Long templateId;
    private String dayOfWeek;
    private LocalDate date;
    private Boolean isHoliday;
    private List<LessonDTO> lessons;

    public ScheduleDayDTO() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Boolean getIsHoliday() { return isHoliday; }
    public void setIsHoliday(Boolean isHoliday) { this.isHoliday = isHoliday; }

    public List<LessonDTO> getLessons() { return lessons; }
    public void setLessons(List<LessonDTO> lessons) { this.lessons = lessons; }
}