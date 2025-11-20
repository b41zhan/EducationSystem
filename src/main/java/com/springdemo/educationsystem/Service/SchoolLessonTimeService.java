package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.Entity.SchoolLessonTime;
import com.springdemo.educationsystem.Repository.SchoolLessonTimeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SchoolLessonTimeService {

    private final SchoolLessonTimeRepository schoolLessonTimeRepository;

    public SchoolLessonTimeService(SchoolLessonTimeRepository schoolLessonTimeRepository) {
        this.schoolLessonTimeRepository = schoolLessonTimeRepository;
    }

    public SchoolLessonTime createLessonTime(SchoolLessonTime lessonTime) {
        return schoolLessonTimeRepository.save(lessonTime);
    }

    public List<SchoolLessonTime> getLessonTimesBySchool(Long schoolId) {
        return schoolLessonTimeRepository.findBySchoolIdOrderByLessonNumber(schoolId);
    }

    public Optional<SchoolLessonTime> getLessonTimeBySchoolAndNumber(Long schoolId, Integer lessonNumber) {
        return schoolLessonTimeRepository.findBySchoolIdAndLessonNumber(schoolId, lessonNumber);
    }

    public SchoolLessonTime updateLessonTime(Long id, SchoolLessonTime lessonTimeDetails) {
        Optional<SchoolLessonTime> optionalLessonTime = schoolLessonTimeRepository.findById(id);
        if (optionalLessonTime.isPresent()) {
            SchoolLessonTime lessonTime = optionalLessonTime.get();
            lessonTime.setLessonNumber(lessonTimeDetails.getLessonNumber());
            lessonTime.setStartTime(lessonTimeDetails.getStartTime());
            lessonTime.setEndTime(lessonTimeDetails.getEndTime());
            lessonTime.setBreakDuration(lessonTimeDetails.getBreakDuration());
            return schoolLessonTimeRepository.save(lessonTime);
        }
        return null;
    }

    public void deleteLessonTime(Long id) {
        schoolLessonTimeRepository.deleteById(id);
    }
}