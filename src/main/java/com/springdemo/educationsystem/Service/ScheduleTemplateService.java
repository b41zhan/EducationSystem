package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.Entity.ScheduleTemplate;
import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Repository.ScheduleTemplateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleTemplateService {

    private final ScheduleTemplateRepository scheduleTemplateRepository;

    public ScheduleTemplateService(ScheduleTemplateRepository scheduleTemplateRepository) {
        this.scheduleTemplateRepository = scheduleTemplateRepository;
    }

    public ScheduleTemplate createTemplate(ScheduleTemplate template) {
        return scheduleTemplateRepository.save(template);
    }

    public List<ScheduleTemplate> getTemplatesByClassAndQuarter(SchoolClass schoolClass, Integer quarter) {
        return scheduleTemplateRepository.findBySchoolClassAndQuarter(schoolClass, quarter);
    }

    public Optional<ScheduleTemplate> getTemplateByClassQuarterAndWeek(SchoolClass schoolClass, Integer quarter, Integer weekNumber) {
        return scheduleTemplateRepository.findBySchoolClassAndQuarterAndWeekNumber(schoolClass, quarter, weekNumber);
    }

    public Optional<ScheduleTemplate> getTemplateByClassAndDate(SchoolClass schoolClass, LocalDate date) {
        return scheduleTemplateRepository.findBySchoolClassAndDate(schoolClass, date);
    }

    public List<ScheduleTemplate> getTemplatesByDate(LocalDate date) {
        return scheduleTemplateRepository.findByDate(date);
    }

    public ScheduleTemplate updateTemplate(Long id, ScheduleTemplate templateDetails) {
        Optional<ScheduleTemplate> optionalTemplate = scheduleTemplateRepository.findById(id);
        if (optionalTemplate.isPresent()) {
            ScheduleTemplate template = optionalTemplate.get();
            template.setQuarter(templateDetails.getQuarter());
            template.setWeekNumber(templateDetails.getWeekNumber());
            template.setWeekStart(templateDetails.getWeekStart());
            template.setWeekEnd(templateDetails.getWeekEnd());
            return scheduleTemplateRepository.save(template);
        }
        return null;
    }

    public void deleteTemplate(Long id) {
        scheduleTemplateRepository.deleteById(id);
    }
}