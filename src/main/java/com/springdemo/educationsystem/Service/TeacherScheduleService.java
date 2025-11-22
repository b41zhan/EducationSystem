package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.CreateTeacherScheduleDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherScheduleService.class);

    private final LessonRepository lessonRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final SchoolClassRepository schoolClassRepository;

    public TeacherScheduleService(LessonRepository lessonRepository,
                                  TeacherRepository teacherRepository,
                                  SubjectRepository subjectRepository,
                                  ScheduleDayRepository scheduleDayRepository,
                                  SchoolClassRepository schoolClassRepository) {
        this.lessonRepository = lessonRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.scheduleDayRepository = scheduleDayRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    @Transactional
    public Lesson createTeacherSchedule(CreateTeacherScheduleDTO scheduleDTO) {
        try {
            // Проверяем существование учителя
            Teacher teacher = teacherRepository.findById(scheduleDTO.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + scheduleDTO.getTeacherId()));

            // Проверяем существование дня расписания
            ScheduleDay day = scheduleDayRepository.findById(scheduleDTO.getDayId())
                    .orElseThrow(() -> new RuntimeException("Schedule day not found with id: " + scheduleDTO.getDayId()));

            // Проверяем существование предмета
            Subject subject = subjectRepository.findById(scheduleDTO.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found with id: " + scheduleDTO.getSubjectId()));

            // Проверяем существование класса (если указан)
            SchoolClass schoolClass = null;
            if (scheduleDTO.getClassId() != null) {
                schoolClass = schoolClassRepository.findById(scheduleDTO.getClassId())
                        .orElseThrow(() -> new RuntimeException("School class not found with id: " + scheduleDTO.getClassId()));
            }

            // Проверяем конфликты расписания для учителя
            checkTeacherScheduleConflict(teacher, day, scheduleDTO.getStartTime(), scheduleDTO.getEndTime());

            // Создаем урок
            Lesson lesson = new Lesson();
            lesson.setDay(day);
            lesson.setLessonNumber(scheduleDTO.getLessonNumber());
            lesson.setStartTime(scheduleDTO.getStartTime());
            lesson.setEndTime(scheduleDTO.getEndTime());
            lesson.setSubject(subject);
            lesson.setTeacher(teacher);
            lesson.setClassroom(scheduleDTO.getClassroom());

            Lesson savedLesson = lessonRepository.save(lesson);

            logger.info("Created teacher schedule: teacher={}, day={}, subject={}, time={}-{}",
                    teacher.getId(), day.getDate(), subject.getName(),
                    scheduleDTO.getStartTime(), scheduleDTO.getEndTime());

            return savedLesson;

        } catch (Exception e) {
            logger.error("Error creating teacher schedule: {}", e.getMessage());
            throw new RuntimeException("Failed to create teacher schedule: " + e.getMessage());
        }
    }

    private void checkTeacherScheduleConflict(Teacher teacher, ScheduleDay day, LocalTime startTime, LocalTime endTime) {
        // Получаем все уроки учителя в этот день
        List<Lesson> teacherLessons = lessonRepository.findByTeacherAndDate(teacher, day.getDate());

        for (Lesson existingLesson : teacherLessons) {
            // Проверяем пересечение временных интервалов
            if (isTimeOverlap(existingLesson.getStartTime(), existingLesson.getEndTime(), startTime, endTime)) {
                throw new RuntimeException(String.format(
                        "Teacher schedule conflict: Teacher already has lesson at %s-%s on %s",
                        existingLesson.getStartTime(), existingLesson.getEndTime(), day.getDate()
                ));
            }
        }
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public List<Lesson> getTeacherSchedule(Long teacherId, LocalDate date) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        return lessonRepository.findByTeacherAndDate(teacher, date);
    }

    public List<Lesson> getTeacherScheduleForWeek(Long teacherId, LocalDate startDate, LocalDate endDate) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        return lessonRepository.findByTeacherAndDateRange(teacher, startDate, endDate);
    }

    @Transactional
    public void deleteTeacherLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + lessonId));

        lessonRepository.delete(lesson);
        logger.info("Deleted teacher lesson: {}", lessonId);
    }

    // Метод для массового создания расписания
    @Transactional
    public void createBulkTeacherSchedule(List<CreateTeacherScheduleDTO> scheduleDTOs) {
        for (CreateTeacherScheduleDTO scheduleDTO : scheduleDTOs) {
            createTeacherSchedule(scheduleDTO);
        }
        logger.info("Created bulk teacher schedule: {} lessons", scheduleDTOs.size());
    }
}