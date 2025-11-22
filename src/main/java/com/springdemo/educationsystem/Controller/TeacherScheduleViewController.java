package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.LessonDTO;
import com.springdemo.educationsystem.Entity.Lesson;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.TeacherScheduleService;
import com.springdemo.educationsystem.Service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher/schedule")
@CrossOrigin("*")
public class TeacherScheduleViewController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherScheduleViewController.class);

    private final TeacherScheduleService teacherScheduleService;
    private final TeacherService teacherService;
    private final AuthService authService;

    public TeacherScheduleViewController(TeacherScheduleService teacherScheduleService,
                                         TeacherService teacherService,
                                         AuthService authService) {
        this.teacherScheduleService = teacherScheduleService;
        this.teacherService = teacherService;
        this.authService = authService;
    }

    // Получить свое расписание на день
    @GetMapping("/my/day")
    public ResponseEntity<?> getMyScheduleForDay(
            @RequestParam(required = false) LocalDate date,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long teacherId = getCurrentTeacherId(token);

        try {
            // Если дата не указана, используем сегодняшнюю
            if (date == null) {
                date = LocalDate.now();
            }

            List<Lesson> lessons = teacherScheduleService.getTeacherSchedule(teacherId, date);
            List<LessonDTO> lessonDTOs = lessons.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(lessonDTOs);
        } catch (Exception e) {
            logger.error("Error getting teacher schedule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Получить свое расписание на неделю
    @GetMapping("/my/week")
    public ResponseEntity<?> getMyScheduleForWeek(
            @RequestParam(required = false) LocalDate startDate,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long teacherId = getCurrentTeacherId(token);

        try {
            // Если начальная дата не указана, используем начало текущей недели
            if (startDate == null) {
                startDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            }
            LocalDate endDate = startDate.plusDays(6);

            List<Lesson> lessons = teacherScheduleService.getTeacherScheduleForWeek(teacherId, startDate, endDate);
            List<LessonDTO> lessonDTOs = lessons.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(lessonDTOs);
        } catch (Exception e) {
            logger.error("Error getting teacher week schedule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long getCurrentTeacherId(String token) {
        Long userId = authService.getUserId(token);
        // Получаем teacherId по userId
        var teacher = teacherService.getTeacherByUserId(userId);
        if (teacher == null) {
            throw new RuntimeException("Teacher not found for user id: " + userId);
        }
        return teacher.getId();
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

    private LessonDTO convertToDTO(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setDayId(lesson.getDay().getId());
        dto.setLessonNumber(lesson.getLessonNumber());
        dto.setStartTime(lesson.getStartTime());
        dto.setEndTime(lesson.getEndTime());
        dto.setSubjectId(lesson.getSubject().getId());
        dto.setSubjectName(lesson.getSubject().getName());
        dto.setClassroom(lesson.getClassroom());

        if (lesson.getTeacher() != null) {
            dto.setTeacherId(lesson.getTeacher().getId());
            if (lesson.getTeacher().getUser() != null) {
                dto.setTeacherName(lesson.getTeacher().getUser().getFirstName() + " " +
                        lesson.getTeacher().getUser().getLastName());
            }
        }

        // Добавляем информацию о классе
        if (lesson.getDay() != null && lesson.getDay().getTemplate() != null) {
            var schoolClass = lesson.getDay().getTemplate().getSchoolClass();
            if (schoolClass != null) {
                dto.setClassName(schoolClass.getName());
            }
        }

        // ДОБАВЛЯЕМ ДЕНЬ НЕДЕЛИ И ДАТУ
        if (lesson.getDay() != null) {
            dto.setDayOfWeek(lesson.getDay().getDayOfWeek().name());
            dto.setDate(lesson.getDay().getDate());
        }

        return dto;
    }
}