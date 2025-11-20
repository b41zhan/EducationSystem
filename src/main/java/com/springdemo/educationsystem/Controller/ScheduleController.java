package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.*;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Service.*;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin("*")
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final ScheduleTemplateService scheduleTemplateService;
    private final ScheduleDayService scheduleDayService;
    private final LessonService lessonService;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository; // ДОБАВЛЕНО
    private final AuthService authService;

    public ScheduleController(ScheduleTemplateService scheduleTemplateService,
                              ScheduleDayService scheduleDayService,
                              LessonService lessonService,
                              SchoolClassRepository schoolClassRepository,
                              SubjectRepository subjectRepository,
                              TeacherRepository teacherRepository,
                              StudentRepository studentRepository, // ДОБАВЛЕНО
                              AuthService authService) {
        this.scheduleTemplateService = scheduleTemplateService;
        this.scheduleDayService = scheduleDayService;
        this.lessonService = lessonService;
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository; // ДОБАВЛЕНО
        this.authService = authService;
    }

    // ==================== ДЛЯ УЧЕНИКОВ ====================

    @GetMapping("/student/my")
    public ResponseEntity<?> getMySchedule(
            @RequestParam(required = false) LocalDate date,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long studentId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"student".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Student only."));
        }

        try {
            // Если дата не указана, используем сегодняшнюю
            if (date == null) {
                date = LocalDate.now();
            }

            // Получаем студента по userId
            Student student = studentRepository.findByUserId(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            if (student.getSchoolClass() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Student class not found"));
            }

            SchoolClass schoolClass = student.getSchoolClass();

            // Получаем расписание на указанную дату
            List<Lesson> lessons = lessonService.getLessonsByClassAndDate(schoolClass, date);

            List<LessonDTO> lessonDTOs = lessons.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(lessonDTOs);

        } catch (Exception e) {
            logger.error("Error getting student schedule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/week")
    public ResponseEntity<?> getMyWeekSchedule(
            @RequestParam LocalDate startDate,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long studentId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"student".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Student only."));
        }

        try {
            // Получаем студента по userId
            Student student = studentRepository.findByUserId(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            if (student.getSchoolClass() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Student class not found"));
            }

            SchoolClass schoolClass = student.getSchoolClass();
            LocalDate endDate = startDate.plusDays(6); // неделя

            List<ScheduleDay> days = scheduleDayService.getDaysByClassAndDateRange(schoolClass, startDate, endDate);

            List<ScheduleDayDTO> dayDTOs = days.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dayDTOs);

        } catch (Exception e) {
            logger.error("Error getting student week schedule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== ДЛЯ АДМИНА ====================

    @PostMapping("/admin/template")
    public ResponseEntity<?> createScheduleTemplate(
            @RequestBody CreateScheduleTemplateDTO createDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        logger.info("Creating schedule template for class: {}", createDTO.getClassId());

        if (!isAdmin(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin rights required."));
        }

        try {
            SchoolClass schoolClass = schoolClassRepository.findById(createDTO.getClassId())
                    .orElseThrow(() -> new RuntimeException("Class not found"));

            ScheduleTemplate template = new ScheduleTemplate();
            template.setSchoolClass(schoolClass);
            template.setQuarter(createDTO.getQuarter());
            template.setWeekNumber(createDTO.getWeekNumber());
            template.setWeekStart(createDTO.getWeekStart());
            template.setWeekEnd(createDTO.getWeekEnd());

            ScheduleTemplate savedTemplate = scheduleTemplateService.createTemplate(template);

            // Создаем дни
            if (createDTO.getDays() != null) {
                for (CreateScheduleDayDTO dayDTO : createDTO.getDays()) {
                    ScheduleDay day = new ScheduleDay();
                    day.setTemplate(savedTemplate);
                    day.setDayOfWeek(java.time.DayOfWeek.valueOf(dayDTO.getDayOfWeek()));
                    day.setDate(dayDTO.getDate());
                    day.setIsHoliday(dayDTO.getIsHoliday());

                    ScheduleDay savedDay = scheduleDayService.createDay(day);

                    // Создаем уроки
                    if (dayDTO.getLessons() != null) {
                        for (CreateLessonDTO lessonDTO : dayDTO.getLessons()) {
                            Lesson lesson = new Lesson();
                            lesson.setDay(savedDay);
                            lesson.setLessonNumber(lessonDTO.getLessonNumber());
                            lesson.setStartTime(lessonDTO.getStartTime());
                            lesson.setEndTime(lessonDTO.getEndTime());

                            Subject subject = subjectRepository.findById(lessonDTO.getSubjectId())
                                    .orElseThrow(() -> new RuntimeException("Subject not found"));
                            lesson.setSubject(subject);

                            if (lessonDTO.getTeacherId() != null) {
                                Teacher teacher = teacherRepository.findById(lessonDTO.getTeacherId())
                                        .orElseThrow(() -> new RuntimeException("Teacher not found"));
                                lesson.setTeacher(teacher);
                            }

                            lesson.setClassroom(lessonDTO.getClassroom());
                            lessonService.createLesson(lesson);
                        }
                    }
                }
            }

            ScheduleTemplateDTO resultDTO = convertToDTO(savedTemplate);
            return ResponseEntity.ok(resultDTO);

        } catch (Exception e) {
            logger.error("Error creating schedule template: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private boolean isAdmin(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        String role = authService.getUserRole(token);
        return "admin".equals(role);
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

    private ScheduleTemplateDTO convertToDTO(ScheduleTemplate template) {
        ScheduleTemplateDTO dto = new ScheduleTemplateDTO();
        dto.setId(template.getId());
        dto.setClassId(template.getSchoolClass().getId());
        dto.setClassName(template.getSchoolClass().getName());
        dto.setQuarter(template.getQuarter());
        dto.setWeekNumber(template.getWeekNumber());
        dto.setWeekStart(template.getWeekStart());
        dto.setWeekEnd(template.getWeekEnd());

        // Конвертируем дни
        List<ScheduleDay> days = scheduleDayService.getDaysByTemplate(template);
        List<ScheduleDayDTO> dayDTOs = days.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dto.setDays(dayDTOs);

        return dto;
    }

    private ScheduleDayDTO convertToDTO(ScheduleDay day) {
        ScheduleDayDTO dto = new ScheduleDayDTO();
        dto.setId(day.getId());
        dto.setTemplateId(day.getTemplate().getId());
        dto.setDayOfWeek(day.getDayOfWeek().name());
        dto.setDate(day.getDate());
        dto.setIsHoliday(day.getIsHoliday());

        // Конвертируем уроки
        List<Lesson> lessons = lessonService.getLessonsByDay(day);
        List<LessonDTO> lessonDTOs = lessons.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dto.setLessons(lessonDTOs);

        return dto;
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

        return dto;
    }
}