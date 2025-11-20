package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.Entity.ScheduleDay;
import com.springdemo.educationsystem.Entity.ScheduleTemplate;
import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Repository.ScheduleDayRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleDayService {

    private final ScheduleDayRepository scheduleDayRepository;

    public ScheduleDayService(ScheduleDayRepository scheduleDayRepository) {
        this.scheduleDayRepository = scheduleDayRepository;
    }

    public ScheduleDay createDay(ScheduleDay day) {
        return scheduleDayRepository.save(day);
    }

    public List<ScheduleDay> getDaysByTemplate(ScheduleTemplate template) {
        return scheduleDayRepository.findByTemplate(template);
    }

    public Optional<ScheduleDay> getDayByDate(LocalDate date) {
        return scheduleDayRepository.findByDate(date);
    }

    public Optional<ScheduleDay> getDayByClassAndDate(SchoolClass schoolClass, LocalDate date) {
        return scheduleDayRepository.findBySchoolClassAndDate(schoolClass, date);
    }

    public List<ScheduleDay> getDaysByClassAndDateRange(SchoolClass schoolClass, LocalDate startDate, LocalDate endDate) {
        return scheduleDayRepository.findBySchoolClassAndDateRange(schoolClass, startDate, endDate);
    }

    public ScheduleDay updateDay(Long id, ScheduleDay dayDetails) {
        Optional<ScheduleDay> optionalDay = scheduleDayRepository.findById(id);
        if (optionalDay.isPresent()) {
            ScheduleDay day = optionalDay.get();
            day.setDate(dayDetails.getDate());
            day.setDayOfWeek(dayDetails.getDayOfWeek());
            day.setIsHoliday(dayDetails.getIsHoliday());
            return scheduleDayRepository.save(day);
        }
        return null;
    }

    public void deleteDay(Long id) {
        scheduleDayRepository.deleteById(id);
    }
}