package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.Entity.Lesson;
import com.springdemo.educationsystem.Entity.ScheduleDay;
import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Entity.Teacher;
import com.springdemo.educationsystem.Repository.LessonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public Lesson createLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }

    public List<Lesson> getLessonsByDay(ScheduleDay day) {
        return lessonRepository.findByDayOrderByLessonNumber(day);
    }

    public List<Lesson> getLessonsByTeacherAndDate(Teacher teacher, LocalDate date) {
        return lessonRepository.findByTeacherAndDate(teacher, date);
    }

    public List<Lesson> getLessonsByTeacherAndDateRange(Teacher teacher, LocalDate startDate, LocalDate endDate) {
        return lessonRepository.findByTeacherAndDateRange(teacher, startDate, endDate);
    }

    public List<Lesson> getLessonsByClassAndDate(SchoolClass schoolClass, LocalDate date) {
        return lessonRepository.findBySchoolClassAndDate(schoolClass, date);
    }

    public Lesson updateLesson(Long id, Lesson lessonDetails) {
        Optional<Lesson> optionalLesson = lessonRepository.findById(id);
        if (optionalLesson.isPresent()) {
            Lesson lesson = optionalLesson.get();
            lesson.setLessonNumber(lessonDetails.getLessonNumber());
            lesson.setStartTime(lessonDetails.getStartTime());
            lesson.setEndTime(lessonDetails.getEndTime());
            lesson.setSubject(lessonDetails.getSubject());
            lesson.setTeacher(lessonDetails.getTeacher());
            lesson.setClassroom(lessonDetails.getClassroom());
            return lessonRepository.save(lesson);
        }
        return null;
    }

    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }
}