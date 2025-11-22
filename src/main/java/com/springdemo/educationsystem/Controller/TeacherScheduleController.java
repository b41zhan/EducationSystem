package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.BulkScheduleDTO;
import com.springdemo.educationsystem.DTO.CreateTeacherScheduleDTO;
import com.springdemo.educationsystem.DTO.LessonDTO;
import com.springdemo.educationsystem.Entity.Lesson;
import com.springdemo.educationsystem.Entity.ScheduleDay;
import com.springdemo.educationsystem.Entity.ScheduleTemplate;
import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Repository.ScheduleDayRepository;
import com.springdemo.educationsystem.Repository.ScheduleTemplateRepository;
import com.springdemo.educationsystem.Repository.SchoolClassRepository;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.ScheduleDayService;
import com.springdemo.educationsystem.Service.TeacherScheduleService;
import com.springdemo.educationsystem.Service.LessonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/schedule")
@CrossOrigin("*")
public class TeacherScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherScheduleController.class);

    private final TeacherScheduleService teacherScheduleService;
    private final LessonService lessonService;
    private final AuthService authService;
    private final SchoolClassRepository schoolClassRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final ScheduleTemplateRepository scheduleTemplateRepository;
    private final ScheduleDayService scheduleDayService;

    public TeacherScheduleController(TeacherScheduleService teacherScheduleService,
                                     LessonService lessonService,
                                     AuthService authService,
                                     SchoolClassRepository schoolClassRepository,
                                     ScheduleDayRepository scheduleDayRepository,
                                     ScheduleTemplateRepository scheduleTemplateRepository,
                                     ScheduleDayService scheduleDayService) {
        this.teacherScheduleService = teacherScheduleService;
        this.lessonService = lessonService;
        this.authService = authService;
        this.schoolClassRepository = schoolClassRepository;
        this.scheduleDayRepository = scheduleDayRepository;
        this.scheduleTemplateRepository = scheduleTemplateRepository;
        this.scheduleDayService = scheduleDayService;
    }

    // Создать расписание для учителя
    @PostMapping("/teacher")
    public ResponseEntity<?> createTeacherSchedule(
            @RequestBody CreateTeacherScheduleDTO scheduleDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        logger.info("Creating teacher schedule: teacherId={}, dayId={}",
                scheduleDTO.getTeacherId(), scheduleDTO.getDayId());

        if (!isAdmin(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin rights required."));
        }

        try {
            Lesson lesson = teacherScheduleService.createTeacherSchedule(scheduleDTO);
            LessonDTO lessonDTO = convertToDTO(lesson);
            return ResponseEntity.ok(lessonDTO);
        } catch (Exception e) {
            logger.error("Error creating teacher schedule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Массовое создание расписания для учителей
    @PostMapping("/teacher/bulk")
    public ResponseEntity<?> createBulkTeacherSchedule(
            @RequestBody BulkScheduleDTO bulkDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        logger.info("Creating bulk teacher schedule: {} lessons",
                bulkDTO.getSchedules().size());

        if (!isAdmin(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin rights required."));
        }

        try {
            teacherScheduleService.createBulkTeacherSchedule(bulkDTO.getSchedules());
            return ResponseEntity.ok(Map.of("message", "Bulk teacher schedule created successfully"));
        } catch (Exception e) {
            logger.error("Error creating bulk teacher schedule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Получить расписание учителя
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getTeacherSchedule(
            @PathVariable Long teacherId,
            @RequestParam(required = false) LocalDate date,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!isAdmin(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin rights required."));
        }

        try {
            List<Lesson> lessons;
            if (date != null) {
                lessons = teacherScheduleService.getTeacherSchedule(teacherId, date);
            } else {
                // Если дата не указана, возвращаем расписание на текущую неделю
                LocalDate startDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
                LocalDate endDate = startDate.plusDays(6);
                lessons = teacherScheduleService.getTeacherScheduleForWeek(teacherId, startDate, endDate);
            }

            List<LessonDTO> lessonDTOs = lessons.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(lessonDTOs);
        } catch (Exception e) {
            logger.error("Error getting teacher schedule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Удалить урок из расписания учителя
    @DeleteMapping("/teacher/lesson/{lessonId}")
    public ResponseEntity<?> deleteTeacherLesson(
            @PathVariable Long lessonId,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!isAdmin(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin rights required."));
        }

        try {
            teacherScheduleService.deleteTeacherLesson(lessonId);
            return ResponseEntity.ok(Map.of("message", "Lesson deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting teacher lesson: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Обновить существующий урок (связь с учителем)
    @PutMapping("/teacher/lesson/{lessonId}")
    public ResponseEntity<?> updateTeacherLesson(
            @PathVariable Long lessonId,
            @RequestBody CreateTeacherScheduleDTO scheduleDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!isAdmin(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin rights required."));
        }

        try {
            // Сначала удаляем старый урок
            teacherScheduleService.deleteTeacherLesson(lessonId);
            // Затем создаем новый с обновленными данными
            Lesson lesson = teacherScheduleService.createTeacherSchedule(scheduleDTO);
            LessonDTO lessonDTO = convertToDTO(lesson);
            return ResponseEntity.ok(lessonDTO);
        } catch (Exception e) {
            logger.error("Error updating teacher lesson: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private boolean isAdmin(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        String role = authService.getUserRole(token);
        return "admin".equals(role);
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

    @PostMapping("/day-id")
    public ResponseEntity<?> createOrGetDayId(
            @RequestBody Map<String, Object> requestData,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!isAdmin(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin rights required."));
        }

        try {
            LocalDate date = LocalDate.parse(requestData.get("date").toString());
            Long classId = Long.valueOf(requestData.get("classId").toString());

            var schoolClass = schoolClassRepository.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Class not found"));

            // Пытаемся найти существующий день
            Optional<ScheduleDay> existingDay = scheduleDayRepository.findBySchoolClassAndDate(schoolClass, date);

            if (existingDay.isPresent()) {
                return ResponseEntity.ok(Map.of("dayId", existingDay.get().getId()));
            } else {
                // Создаем новый день расписания
                return createNewScheduleDay(date, schoolClass);
            }

        } catch (Exception e) {
            logger.error("Error creating/finding day id: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private ResponseEntity<?> createNewScheduleDay(LocalDate date, SchoolClass schoolClass) {
        try {
            // Находим или создаем шаблон для этой даты
            ScheduleTemplate template = findOrCreateTemplate(date, schoolClass);

            // Создаем день расписания
            ScheduleDay newDay = new ScheduleDay();
            newDay.setTemplate(template);
            newDay.setDayOfWeek(date.getDayOfWeek());
            newDay.setDate(date);
            newDay.setIsHoliday(false);

            ScheduleDay savedDay = scheduleDayService.createDay(newDay);

            logger.info("Created new schedule day: id={}, date={}, class={}",
                    savedDay.getId(), date, schoolClass.getName());

            return ResponseEntity.ok(Map.of("dayId", savedDay.getId()));

        } catch (Exception e) {
            logger.error("Error creating new schedule day: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create schedule day: " + e.getMessage()));
        }
    }

    private ScheduleTemplate findOrCreateTemplate(LocalDate date, SchoolClass schoolClass) {
        // Пытаемся найти существующий шаблон для этой даты
        Optional<ScheduleTemplate> existingTemplate = scheduleTemplateRepository.findBySchoolClassAndDate(schoolClass, date);

        if (existingTemplate.isPresent()) {
            return existingTemplate.get();
        }

        // Создаем новый шаблон
        ScheduleTemplate newTemplate = new ScheduleTemplate();
        newTemplate.setSchoolClass(schoolClass);
        newTemplate.setQuarter(calculateQuarter(date)); // Метод для расчета четверти
        newTemplate.setWeekNumber(calculateWeekNumber(date)); // Метод для расчета недели
        newTemplate.setWeekStart(date.with(DayOfWeek.MONDAY));
        newTemplate.setWeekEnd(date.with(DayOfWeek.SUNDAY));

        return scheduleTemplateRepository.save(newTemplate);
    }

    private Integer calculateQuarter(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 9 || month <= 1) return 2; // 2 четверть (сентябрь-январь)
        else if (month >= 2 && month <= 4) return 3; // 3 четверть
        else return 4; // 4 четверть
    }

    private Integer calculateWeekNumber(LocalDate date) {
        // Простая логика расчета номера недели в четверти
        LocalDate quarterStart = LocalDate.of(date.getYear(), 9, 1); // Начало учебного года
        long weeks = java.time.temporal.ChronoUnit.WEEKS.between(quarterStart, date);
        return (int) weeks + 1;
    }

    @GetMapping("/day-id")
    public ResponseEntity<?> getDayIdByDate(
            @RequestParam LocalDate date,
            @RequestParam Long classId,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!isAdmin(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin rights required."));
        }

        try {
            // Находим день расписания по дате и классу
            var schoolClass = schoolClassRepository.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Class not found"));

            var scheduleDay = scheduleDayRepository.findBySchoolClassAndDate(schoolClass, date)
                    .orElseThrow(() -> new RuntimeException("Schedule day not found for date: " + date));

            return ResponseEntity.ok(Map.of("dayId", scheduleDay.getId()));
        } catch (Exception e) {
            logger.error("Error finding day id: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}