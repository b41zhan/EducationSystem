package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.*;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.TeacherJournalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/journal/teacher")
@CrossOrigin("*")
public class TeacherJournalController {

    private final TeacherJournalService teacherJournalService;
    private final AuthService authService;

    public TeacherJournalController(TeacherJournalService teacherJournalService, AuthService authService) {
        this.teacherJournalService = teacherJournalService;
        this.authService = authService;
    }

    @GetMapping("/my-classes-subjects")
    public ResponseEntity<?> getMyClassesSubjects(@RequestHeader("Authorization") String authorizationHeader) {
        Long userId = requireTeacher(authorizationHeader);
        return ResponseEntity.ok(teacherJournalService.getTeacherJournalClasses(userId));
    }

    @GetMapping
    public ResponseEntity<?> getJournal(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam Integer quarter
    ) {
        Long userId = requireTeacher(authorizationHeader);
        return ResponseEntity.ok(teacherJournalService.getJournal(userId, classId, subjectId, quarter));
    }

    @PutMapping("/attendance")
    public ResponseEntity<?> toggleAttendance(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UpsertAttendanceDTO request
    ) {
        Long userId = requireTeacher(authorizationHeader);
        teacherJournalService.toggleAttendance(userId, request);
        return ResponseEntity.ok(Map.of("message", "Attendance toggled"));
    }

    @PutMapping("/lesson-grade")
    public ResponseEntity<?> upsertLessonGrade(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UpsertLessonGradeDTO request
    ) {
        Long userId = requireTeacher(authorizationHeader);
        teacherJournalService.upsertLessonGrade(userId, request);
        return ResponseEntity.ok(Map.of("message", "Lesson grade saved"));
    }

    @PutMapping("/quarter-final")
    public ResponseEntity<?> upsertQuarterFinal(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UpsertFinalGradeDTO request
    ) {
        Long userId = requireTeacher(authorizationHeader);
        teacherJournalService.upsertQuarterFinalGrade(userId, request);
        return ResponseEntity.ok(Map.of("message", "Quarter final saved"));
    }

    @PutMapping("/year-final")
    public ResponseEntity<?> upsertYearFinal(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UpsertFinalGradeDTO request
    ) {
        Long userId = requireTeacher(authorizationHeader);
        teacherJournalService.upsertYearFinalGrade(userId, request);
        return ResponseEntity.ok(Map.of("message", "Year final saved"));
    }

    @PostMapping("/calculate-quarter-final")
    public ResponseEntity<?> calculateQuarterFinal(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam Long studentId,
            @RequestParam Integer quarter
    ) {
        Long userId = requireTeacher(authorizationHeader);
        return ResponseEntity.ok(
                teacherJournalService.calculateQuarterFinal(userId, classId, subjectId, studentId, quarter)
        );
    }

    @PostMapping("/calculate-year-final")
    public ResponseEntity<?> calculateYearFinal(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam Long studentId
    ) {
        Long userId = requireTeacher(authorizationHeader);
        return ResponseEntity.ok(
                teacherJournalService.calculateYearFinal(userId, classId, subjectId, studentId)
        );
    }

    @PostMapping("/sync-assignments")
    public ResponseEntity<?> syncAssignments(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam Integer quarter
    ) {
        Long userId = requireTeacher(authorizationHeader);
        teacherJournalService.syncAssignments(userId, classId, subjectId, quarter);
        return ResponseEntity.ok(Map.of("message", "Assignments synced"));
    }

    @PostMapping("/sync-quizzes")
    public ResponseEntity<?> syncQuizzes(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam Integer quarter
    ) {
        Long userId = requireTeacher(authorizationHeader);
        teacherJournalService.syncQuizzes(userId, classId, subjectId, quarter);
        return ResponseEntity.ok(Map.of("message", "Quizzes synced"));
    }

    private Long requireTeacher(String authorizationHeader) {
        String token = authorizationHeader.substring(7);

        if (!authService.isValidToken(token)) {
            throw new RuntimeException("Authentication required");
        }

        String role = authService.getUserRole(token);
        if (!"teacher".equals(role)) {
            throw new RuntimeException("Only teachers can access journal");
        }

        return authService.getUserId(token);
    }
}