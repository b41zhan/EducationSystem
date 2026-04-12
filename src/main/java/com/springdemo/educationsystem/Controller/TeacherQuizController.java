package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.CreateQuestionDTO;
import com.springdemo.educationsystem.DTO.CreateQuizAssignmentDTO;
import com.springdemo.educationsystem.DTO.CreateQuizDTO;
import com.springdemo.educationsystem.DTO.GradeTextQuizAnswersDTO;
import com.springdemo.educationsystem.DTO.TeacherQuizAttemptDetailsDTO;
import com.springdemo.educationsystem.Entity.Quiz;
import com.springdemo.educationsystem.Entity.QuizAssignment;
import com.springdemo.educationsystem.Entity.QuizAttempt;
import com.springdemo.educationsystem.Entity.QuizQuestion;
import com.springdemo.educationsystem.Entity.Teacher;
import com.springdemo.educationsystem.Repository.QuizAttemptRepository;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.QuizAssignmentService;
import com.springdemo.educationsystem.Service.QuizService;
import com.springdemo.educationsystem.Service.TeacherQuizReviewService;
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
    private final QuizAssignmentService quizAssignmentService;
    private final QuizAttemptRepository attemptRepository;
    private final TeacherQuizReviewService teacherQuizReviewService;

    public TeacherQuizController(
            QuizService quizService,
            AuthService authService,
            TeacherService teacherService,
            QuizAssignmentService quizAssignmentService,
            QuizAttemptRepository attemptRepository,
            TeacherQuizReviewService teacherQuizReviewService
    ) {
        this.quizService = quizService;
        this.authService = authService;
        this.teacherService = teacherService;
        this.quizAssignmentService = quizAssignmentService;
        this.attemptRepository = attemptRepository;
        this.teacherQuizReviewService = teacherQuizReviewService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createQuiz(
            @RequestBody CreateQuizDTO dto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            Teacher teacher = getAuthorizedTeacher(authorizationHeader);
            Quiz createdQuiz = quizService.createQuiz(dto, teacher.getId());
            return ResponseEntity.ok(createdQuiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{quizId}/question")
    public ResponseEntity<?> addQuestion(
            @PathVariable Long quizId,
            @RequestBody CreateQuestionDTO dto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            Teacher teacher = getAuthorizedTeacher(authorizationHeader);
            QuizQuestion createdQuestion = quizService.addQuestion(quizId, dto, teacher.getId());
            return ResponseEntity.ok(createdQuestion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyQuizzes(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            Teacher teacher = getAuthorizedTeacher(authorizationHeader);
            return ResponseEntity.ok(quizService.getTeacherQuizzes(teacher.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignQuiz(
            @RequestBody CreateQuizAssignmentDTO dto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            Teacher teacher = getAuthorizedTeacher(authorizationHeader);
            QuizAssignment createdAssignment = quizAssignmentService.createAssignment(teacher.getId(), dto);
            return ResponseEntity.ok(createdAssignment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/assignments")
    public ResponseEntity<?> getMyAssignments(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            Teacher teacher = getAuthorizedTeacher(authorizationHeader);
            return ResponseEntity.ok(quizAssignmentService.getTeacherAssignments(teacher.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/results/{assignmentId}")
    public ResponseEntity<?> getResults(
            @PathVariable Long assignmentId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            Teacher teacher = getAuthorizedTeacher(authorizationHeader);

            List<QuizAttempt> attempts = attemptRepository.findByQuizAssignmentId(assignmentId);

            boolean hasForeignAttempt = attempts.stream().anyMatch(a ->
                    a.getQuizAssignment() == null ||
                            a.getQuizAssignment().getTeacher() == null ||
                            !a.getQuizAssignment().getTeacher().getId().equals(teacher.getId())
            );

            if (hasForeignAttempt) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You can view only your own assignment results"));
            }

            List<Map<String, Object>> result = attempts.stream().map(a -> {
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

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<?> getAttemptDetails(
            @PathVariable Long attemptId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            Teacher teacher = getAuthorizedTeacher(authorizationHeader);
            TeacherQuizAttemptDetailsDTO dto =
                    teacherQuizReviewService.getAttemptDetails(attemptId, teacher.getId());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/attempt/grade-text")
    public ResponseEntity<?> gradeTextAnswers(
            @RequestBody GradeTextQuizAnswersDTO request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            Teacher teacher = getAuthorizedTeacher(authorizationHeader);
            TeacherQuizAttemptDetailsDTO dto =
                    teacherQuizReviewService.gradeTextAnswers(request, teacher.getId());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Teacher getAuthorizedTeacher(String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        if (!authService.isValidToken(token)) {
            throw new RuntimeException("Authentication required");
        }

        String userRole = authService.getUserRole(token);
        if (!"teacher".equals(userRole)) {
            throw new RuntimeException("Only teachers can access quiz teacher endpoints");
        }

        Long userId = authService.getUserId(token);
        Teacher teacher = teacherService.getTeacherByUserId(userId);

        if (teacher == null) {
            throw new RuntimeException("Teacher not found");
        }

        return teacher;
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header is missing");
        }
        return authorizationHeader.substring(7);
    }
}