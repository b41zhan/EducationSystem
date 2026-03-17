package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.FinishQuizDTO;
import com.springdemo.educationsystem.DTO.SaveAnswerDTO;
import com.springdemo.educationsystem.DTO.StartQuizDTO;
import com.springdemo.educationsystem.Entity.QuizAnswer;
import com.springdemo.educationsystem.Entity.QuizAssignment;
import com.springdemo.educationsystem.Entity.QuizAttempt;
import com.springdemo.educationsystem.Repository.QuizAttemptRepository;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.QuizAssignmentService;
import com.springdemo.educationsystem.Service.QuizAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/student/quiz")
@CrossOrigin("*")
public class StudentQuizController {

    private final QuizAttemptService quizAttemptService;
    private final QuizAssignmentService quizAssignmentService;
    private final AuthService authService;
    private final QuizAttemptRepository attemptRepository;

    public StudentQuizController(
            QuizAttemptService quizAttemptService,
            QuizAssignmentService quizAssignmentService,
            AuthService authService,
            QuizAttemptRepository attemptRepository
    ) {
        this.quizAttemptService = quizAttemptService;
        this.quizAssignmentService = quizAssignmentService;
        this.authService = authService;
        this.attemptRepository = attemptRepository;
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableQuizzes(
            @RequestHeader("Authorization") String auth
    ) {
        String token = auth.substring(7);

        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).build();
        }

        Long studentId = authService.getUserId(token);

        return ResponseEntity.ok(
                quizAssignmentService.getAvailableAssignmentsForStudent(studentId)
        );
    }

    @GetMapping("/api/student/quiz/result/{attemptId}")
    public Map<String, Object> getStudentResult(@PathVariable Long attemptId){

        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        Map<String, Object> map = new HashMap<>();

        map.put("score", attempt.getScore());
        map.put("status", attempt.getStatus().name());
        map.put("startTime", attempt.getStartTime());
        map.put("endTime", attempt.getEndTime());

        return map;
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getQuizAssignment(
            @PathVariable Long assignmentId,
            @RequestHeader("Authorization") String auth
    ) {
        String token = auth.substring(7);

        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).build();
        }

        Long studentId = authService.getUserId(token);

        QuizAssignment assignment = quizAssignmentService.getAssignmentForStudent(assignmentId, studentId);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/start")
    public ResponseEntity<?> startQuiz(
            @RequestBody StartQuizDTO dto,
            @RequestHeader("Authorization") String auth
    ) {
        String token = auth.substring(7);

        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).build();
        }

        Long studentId = authService.getUserId(token);

        QuizAttempt attempt = quizAttemptService.startAttempt(dto.getAssignmentId(), studentId);

        return ResponseEntity.ok(attempt);
    }

    @PostMapping("/answer")
    public ResponseEntity<?> saveAnswer(
            @RequestBody SaveAnswerDTO dto
    ) {
        QuizAnswer answer = quizAttemptService.saveAnswer(dto);
        return ResponseEntity.ok(answer);
    }

    @PostMapping("/finish")
    public ResponseEntity<?> finishQuiz(
            @RequestBody FinishQuizDTO dto
    ) {
        QuizAttempt attempt = quizAttemptService.finishAttempt(dto.getAttemptId());
        return ResponseEntity.ok(attempt);
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<?> getAttempt(
            @PathVariable Long attemptId
    ) {
        return ResponseEntity.ok(
                quizAttemptService.getAttempt(attemptId)
        );
    }
}