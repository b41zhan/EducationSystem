package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.Entity.Parent;
import com.springdemo.educationsystem.Entity.ParentStudent;
import com.springdemo.educationsystem.Entity.Student;
import com.springdemo.educationsystem.Repository.GradeRepository;
import com.springdemo.educationsystem.Repository.ParentRepository;
import com.springdemo.educationsystem.Repository.ParentStudentRepository;
import com.springdemo.educationsystem.Repository.StudentRepository;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.LessonService;
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

    private Parent getAuthorizedParent(String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        if (!authService.isValidToken(token)) {
            throw new RuntimeException("Authentication required");
        }

        Long parentUserId = authService.getUserId(token);

        return parentRepository.findById(parentUserId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));
    }

    private Student getLinkedStudentOrThrow(Parent parent, Long studentId) {
        boolean linked = parentStudentRepository.existsByParentIdAndStudentId(parent.getId(), studentId);

        if (!linked) {
            throw new RuntimeException("This child is not linked to this parent");
        }

        return studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    @GetMapping("/children")
    public ResponseEntity<?> getMyChildren(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            Parent parent = getAuthorizedParent(authorizationHeader);

            List<ParentStudent> links = parentStudentRepository.findByParentId(parent.getId());
            if (links == null || links.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<Map<String, Object>> children = links.stream()
                    .map(ParentStudent::getStudent)
                    .filter(Objects::nonNull)
                    .map(this::toChildCardMap)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(children);

        } catch (RuntimeException e) {
            if ("Authentication required".equals(e.getMessage())) {
                return authFail();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> toChildCardMap(Student s) {
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

        Map<String, Object> result = new HashMap<>();
        result.put("id", s.getId());
        result.put("fio", fio);
        result.put("className", className);
        result.put("schoolName", schoolName);
        return result;
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
        try {
            Parent parent = getAuthorizedParent(authorizationHeader);
            getLinkedStudentOrThrow(parent, studentId);

            var grades = gradeRepository.findByStudentId(studentId);

            grades.sort((a, b) -> {
                if (a.getGradedAt() == null && b.getGradedAt() == null) return 0;
                if (a.getGradedAt() == null) return 1;
                if (b.getGradedAt() == null) return -1;
                return b.getGradedAt().compareTo(a.getGradedAt());
            });

            List<Map<String, Object>> rows = grades.stream()
                    .limit(Math.max(1, limit))
                    .map(g -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("date", g.getGradedAt());
                        row.put("subject",
                                g.getSubmission() != null
                                        && g.getSubmission().getAssignment() != null
                                        && g.getSubmission().getAssignment().getSubject() != null
                                        ? g.getSubmission().getAssignment().getSubject().getName()
                                        : null
                        );
                        row.put("grade", g.getGradeValue());
                        return row;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(rows);

        } catch (RuntimeException e) {
            if ("Authentication required".equals(e.getMessage())) {
                return authFail();
            }
            if ("This child is not linked to this parent".equals(e.getMessage())) {
                return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return authFail();
        }

        return ResponseEntity.ok(List.of(
                Map.of(
                        "title", "Тестовое уведомление",
                        "text", "Это тест",
                        "date", LocalDateTime.now()
                )
        ));
    }

    @GetMapping("/children/{studentId}/schedule")
    public ResponseEntity<?> getChildSchedule(
            @PathVariable Long studentId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String weekStart,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            Parent parent = getAuthorizedParent(authorizationHeader);
            Student student = getLinkedStudentOrThrow(parent, studentId);

            if (student.getSchoolClass() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Student class not found"));
            }

            if (date != null && !date.isBlank()) {
                LocalDate d = LocalDate.parse(date);
                return ResponseEntity.ok(
                        lessonService.getLessonsByClassAndDate(student.getSchoolClass(), d)
                );
            }

            if (weekStart != null && !weekStart.isBlank()) {
                LocalDate monday = LocalDate.parse(weekStart);
                Map<String, Object> result = new LinkedHashMap<>();

                for (int i = 0; i < 7; i++) {
                    LocalDate current = monday.plusDays(i);
                    result.put(
                            current.toString(),
                            lessonService.getLessonsByClassAndDate(student.getSchoolClass(), current)
                    );
                }

                return ResponseEntity.ok(result);
            }

            LocalDate today = LocalDate.now();
            return ResponseEntity.ok(
                    lessonService.getLessonsByClassAndDate(student.getSchoolClass(), today)
            );

        } catch (RuntimeException e) {
            if ("Authentication required".equals(e.getMessage())) {
                return authFail();
            }
            if ("This child is not linked to this parent".equals(e.getMessage())) {
                return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}