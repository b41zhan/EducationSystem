package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.LessonDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.ParentRepository;
import com.springdemo.educationsystem.Repository.ParentStudentRepository;
import com.springdemo.educationsystem.Repository.GradeRepository;
import com.springdemo.educationsystem.Repository.StudentRepository;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parent")
@CrossOrigin("*")
public class ParentController {

    private final AuthService authService;
    private final ParentStudentRepository parentStudentRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final GradeRepository gradeRepository;
    private final LessonService lessonService;

    public ParentController(
            AuthService authService,
            ParentStudentRepository parentStudentRepository,
            StudentRepository studentRepository,
            GradeRepository gradeRepository,
            ParentRepository parentRepository,
            LessonService lessonService
    ) {
        this.authService = authService;
        this.parentStudentRepository = parentStudentRepository;
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
        this.parentRepository = parentRepository;
        this.lessonService = lessonService;
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return "";
    }

    private ResponseEntity<?> authFail() {
        return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
    }

    @GetMapping("/children")
    public ResponseEntity<?> getMyChildren(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) return authFail();

        Long parentUserId = authService.getUserId(token);

        List<ParentStudent> links = parentStudentRepository.findByParentUserId(parentUserId);
        if (links == null || links.isEmpty()) return ResponseEntity.ok(List.of());

        List<ChildCardDTO> children = links.stream()
                .map(ParentStudent::getStudent)
                .filter(Objects::nonNull)
                .map(this::toChildCard)
                .collect(Collectors.toList());

        return ResponseEntity.ok(children);
    }

    private ChildCardDTO toChildCard(Student s) {
        String fio = "";
        String className = null;
        String schoolName = null;

        if (s.getUser() != null) {
            fio = String.format("%s %s%s",
                    safe(s.getUser().getLastName()),
                    safe(s.getUser().getFirstName()),
                    s.getUser().getPatronymic() != null ? " " + s.getUser().getPatronymic() : ""
            ).trim();
        }

        if (s.getSchoolClass() != null) {
            className = s.getSchoolClass().getName();
            if (s.getSchoolClass().getSchool() != null) {
                schoolName = s.getSchoolClass().getSchool().getName();
            }
        }

        return new ChildCardDTO(s.getId(), fio, className, schoolName);
    }

    private String safe(String x) {
        return x == null ? "" : x;
    }

    @GetMapping("/children/{studentId}/grades")
    public ResponseEntity<?> getChildGrades(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) return authFail();
        Long parentUserId = authService.getUserId(token);

        // проверка доступа: этот student принадлежит родителю
        boolean allowed = parentStudentRepository.existsByParentUserIdAndStudentId(parentUserId, studentId);
        if (!allowed) return ResponseEntity.status(403).body(Map.of("error", "Access denied"));

        // если у тебя нет метода limit — просто возьми все и обрежь
        var grades = gradeRepository.findByStudentId(studentId);

        // последние сверху (если gradedAt есть)
        grades.sort((a,b) -> {
            if (a.getGradedAt() == null && b.getGradedAt() == null) return 0;
            if (a.getGradedAt() == null) return 1;
            if (b.getGradedAt() == null) return -1;
            return b.getGradedAt().compareTo(a.getGradedAt());
        });

        List<GradeRowDTO> rows = grades.stream()
                .limit(Math.max(1, limit))
                .map(g -> new GradeRowDTO(
                        g.getGradedAt(),
                        g.getSubmission() != null && g.getSubmission().getAssignment() != null
                                && g.getSubmission().getAssignment().getSubject() != null
                                ? g.getSubmission().getAssignment().getSubject().getName()
                                : null,
                        g.getGradeValue()
                ))
                .toList();

        return ResponseEntity.ok(rows);
    }


    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) return authFail();

        return ResponseEntity.ok(List.of(
                Map.of("title", "Тестовое уведомление",
                        "text", "Это тест",
                        "date", LocalDateTime.now())
        ));

    }


    @PostMapping("/children/link")
    public ResponseEntity<?> linkChildToParent(
            @Valid @RequestBody com.springdemo.educationsystem.DTO.LinkChildRequest req,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String token = extractToken(authorizationHeader);
            if (!authService.isValidToken(token)) return authFail();

            Long parentUserId = authService.getUserId(token);

            // ✅ Проверяем что Parent реально существует (таблица parents)
            Parent parent = parentRepository.findById(parentUserId)
                    .orElseThrow(() -> new RuntimeException("Parent not found"));

            // ✅ Проверяем ученика
            Student student = studentRepository.findById(req.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // ✅ ВАЖНО: используем ТВОЙ уже существующий метод
            if (parentStudentRepository.existsByParentUserIdAndStudentId(parentUserId, student.getId())) {
                return ResponseEntity.ok(Map.of("message", "Already linked"));
            }

            ParentStudent ps = new ParentStudent();
            ps.setParent(parent);
            ps.setStudent(student);
            parentStudentRepository.save(ps);

            return ResponseEntity.ok(Map.of("message", "Linked"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/children/{studentId}/schedule")
    public ResponseEntity<?> getChildSchedule(
            @PathVariable Long studentId,
            @RequestParam(required = false) String date,        // YYYY-MM-DD
            @RequestParam(required = false) String weekStart,   // YYYY-MM-DD (понедельник)
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            // 1) Auth exactly like other parent endpoints
            String token = extractToken(authorizationHeader);
            if (!authService.isValidToken(token)) return authFail();

            Long parentUserId = authService.getUserId(token);

            // 2) Parent (id == userId because @MapsId)
            Parent parent = parentRepository.findById(parentUserId)
                    .orElseThrow(() -> new RuntimeException("Parent not found"));

            // 3) Check that this student is linked to this parent
            boolean linked = parentStudentRepository.existsByParentIdAndStudentId(parent.getId(), studentId);
            if (!linked) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "This child is not linked to this parent"));
            }

            // 4) Student + class
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            if (student.getSchoolClass() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Student class not found"));
            }

            // ====== DAY MODE (any specific date) ======
            if (date != null && !date.isBlank()) {
                LocalDate d = LocalDate.parse(date);
                List<Lesson> lessons = lessonService.getLessonsByClassAndDate(student.getSchoolClass(), d);

                List<LessonDTO> dto = lessons.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(dto);
            }

            // ====== WEEK MODE (7 days from weekStart) ======
            if (weekStart != null && !weekStart.isBlank()) {
                LocalDate start = LocalDate.parse(weekStart); // Monday
                Map<String, List<LessonDTO>> result = new LinkedHashMap<>();

                for (int i = 0; i < 7; i++) {
                    LocalDate d = start.plusDays(i);
                    List<Lesson> lessons = lessonService.getLessonsByClassAndDate(student.getSchoolClass(), d);

                    List<LessonDTO> dto = lessons.stream()
                            .map(this::convertToDTO)
                            .collect(Collectors.toList());

                    result.put(d.toString(), dto); // "2026-02-12"
                }

                return ResponseEntity.ok(result);
            }

            // ====== DEFAULT (today) ======
            LocalDate today = LocalDate.now();
            List<Lesson> lessons = lessonService.getLessonsByClassAndDate(student.getSchoolClass(), today);

            List<LessonDTO> dto = lessons.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    // helper как в ScheduleController (можно 1:1)
    private LessonDTO convertToDTO(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setLessonNumber(lesson.getLessonNumber());
        dto.setStartTime(lesson.getStartTime());
        dto.setEndTime(lesson.getEndTime());
        dto.setClassroom(lesson.getClassroom());

        if (lesson.getSubject() != null) {
            dto.setSubjectId(lesson.getSubject().getId());
            dto.setSubjectName(lesson.getSubject().getName());
        }

        if (lesson.getTeacher() != null && lesson.getTeacher().getUser() != null) {
            dto.setTeacherId(lesson.getTeacher().getId());
            dto.setTeacherName(
                    lesson.getTeacher().getUser().getFirstName() + " " +
                            lesson.getTeacher().getUser().getLastName()
            );
        }

        if (lesson.getDay() != null) {
            dto.setDayId(lesson.getDay().getId());
            dto.setDate(lesson.getDay().getDate());
            if (lesson.getDay().getDayOfWeek() != null) {
                dto.setDayOfWeek(lesson.getDay().getDayOfWeek().toString());
            }
            if (lesson.getDay().getTemplate() != null &&
                    lesson.getDay().getTemplate().getSchoolClass() != null) {
                dto.setClassName(lesson.getDay().getTemplate().getSchoolClass().getName());
            }
        }

        return dto;
    }




    public record ChildCardDTO(Long id, String fio, String className, String schoolName) {}
    public record GradeRowDTO(java.time.LocalDateTime date, String subject, Integer grade) {}
}
