package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.CreateQuestionDTO;
import com.springdemo.educationsystem.DTO.CreateQuizAssignmentDTO;
import com.springdemo.educationsystem.DTO.CreateQuizDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.QuizAttemptRepository;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.QuizAssignmentService;
import com.springdemo.educationsystem.Service.QuizAttemptService;
import com.springdemo.educationsystem.Service.QuizService;
import com.springdemo.educationsystem.Service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/quiz")
@CrossOrigin("*")
public class TeacherQuizController {

    private final QuizService quizService;
    private final AuthService authService;
    private final TeacherService teacherService;
    private final QuizAttemptService quizAttemptService;
    private final QuizAssignmentService quizAssignmentService;
    private final QuizAttemptRepository attemptRepository;

    public TeacherQuizController(
            QuizService quizService,
            AuthService authService,
            TeacherService teacherService,
            QuizAttemptService quizAttemptService,
            QuizAssignmentService quizAssignmentService,
            QuizAttemptRepository attemptRepository
    ) {
        this.quizService = quizService;
        this.authService = authService;
        this.teacherService = teacherService;
        this.quizAttemptService = quizAttemptService;
        this.quizAssignmentService = quizAssignmentService;
        this.attemptRepository = attemptRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createQuiz(
            @RequestBody CreateQuizDTO dto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = extractToken(authorizationHeader);

        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String userRole = authService.getUserRole(token);
        if (!"teacher".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers can create quizzes"));
        }

        Long userId = authService.getUserId(token);
        Teacher teacher = teacherService.getTeacherByUserId(userId);

        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        try {
            Quiz createdQuiz = quizService.createQuiz(dto, teacher.getId());
            return ResponseEntity.ok(createdQuiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/results/{assignmentId}")
    public List<Map<String, Object>> getResults(@PathVariable Long assignmentId){

        List<QuizAttempt> attempts = attemptRepository.findByQuizAssignmentId(assignmentId);

        return attempts.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();

            map.put("attemptId", a.getId());
            map.put("studentName",
                    a.getStudent().getUser().getFirstName() + " " +
                            a.getStudent().getUser().getLastName()
            );
            map.put("score", a.getScore());
            map.put("status", a.getStatus().name());

            return map;
        }).toList();
    }

    @PostMapping("/{quizId}/question")
    public ResponseEntity<?> addQuestion(
            @PathVariable Long quizId,
            @RequestBody CreateQuestionDTO dto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = extractToken(authorizationHeader);

        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String userRole = authService.getUserRole(token);
        if (!"teacher".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers can add questions"));
        }

        Long userId = authService.getUserId(token);
        Teacher teacher = teacherService.getTeacherByUserId(userId);

        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        try {
            QuizQuestion question = quizService.addQuestion(quizId, dto, teacher.getId());
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyQuizzes(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = extractToken(authorizationHeader);

        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String userRole = authService.getUserRole(token);
        if (!"teacher".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers can view quizzes"));
        }

        Long userId = authService.getUserId(token);
        Teacher teacher = teacherService.getTeacherByUserId(userId);

        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        try {
            List<Quiz> quizzes = quizService.getTeacherQuizzes(teacher.getId());
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignQuiz(
            @RequestBody CreateQuizAssignmentDTO dto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = extractToken(authorizationHeader);

        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String userRole = authService.getUserRole(token);
        if (!"teacher".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers can assign quizzes"));
        }

        Long userId = authService.getUserId(token);
        Teacher teacher = teacherService.getTeacherByUserId(userId);

        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        try {
            QuizAssignment assignment = quizAssignmentService.createAssignment(teacher.getId(), dto);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/assignments")
    public ResponseEntity<?> getTeacherAssignments(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = extractToken(authorizationHeader);

        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String userRole = authService.getUserRole(token);
        if (!"teacher".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers can view assignments"));
        }

        Long userId = authService.getUserId(token);
        Teacher teacher = teacherService.getTeacherByUserId(userId);

        if (teacher == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        try {
            return ResponseEntity.ok(quizAssignmentService.getTeacherAssignments(teacher.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/assignment/{assignmentId}/attempts")
    public ResponseEntity<?> getAttempts(
            @PathVariable Long assignmentId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = extractToken(authorizationHeader);

        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        return ResponseEntity.ok(
                quizAttemptService.getAttemptsForAssignment(assignmentId)
        );
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
}