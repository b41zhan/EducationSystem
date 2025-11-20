package com.springdemo.educationsystem.DTO;

import java.time.LocalDate;
import java.util.List;

public class CreateScheduleDayDTO {
    private String dayOfWeek;
    private LocalDate date;
    private Boolean isHoliday;
    private List<CreateLessonDTO> lessons;

    public CreateScheduleDayDTO() {}

    // Геттеры и сеттеры
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Boolean getIsHoliday() { return isHoliday; }
    public void setIsHoliday(Boolean isHoliday) { this.isHoliday = isHoliday; }

    public List<CreateLessonDTO> getLessons() { return lessons; }
    public void setLessons(List<CreateLessonDTO> lessons) { this.lessons = lessons; }
}