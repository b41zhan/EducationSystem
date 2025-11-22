package com.springdemo.educationsystem.DTO;

import java.util.List;

public class BulkScheduleDTO {
    private List<CreateTeacherScheduleDTO> schedules;
    private Boolean updateTeacherSchedule = true;

    public BulkScheduleDTO() {}

    // Геттеры и сеттеры
    public List<CreateTeacherScheduleDTO> getSchedules() { return schedules; }
    public void setSchedules(List<CreateTeacherScheduleDTO> schedules) { this.schedules = schedules; }
    public Boolean getUpdateTeacherSchedule() { return updateTeacherSchedule; }
    public void setUpdateTeacherSchedule(Boolean updateTeacherSchedule) { this.updateTeacherSchedule = updateTeacherSchedule; }
}