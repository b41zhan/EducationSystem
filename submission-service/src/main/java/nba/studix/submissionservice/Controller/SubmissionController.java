package nba.studix.submissionservice.Controller;

import nba.studix.submissionservice.DTO.SubmissionDTO;
import nba.studix.submissionservice.DTO.GradeDTO;
import nba.studix.submissionservice.DTO.CreateSubmissionDTO;
import nba.studix.submissionservice.Service.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin("*")
public class SubmissionController {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<?> createSubmission(@RequestBody CreateSubmissionDTO submissionData,
                                              @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить studentId из токена (временная заглушка)
            Long studentId = 3L; // Заглушка для student ID

            submissionService.createSubmission(submissionData, studentId);
            return ResponseEntity.ok(Map.of("message", "Assignment submitted successfully"));
        } catch (Exception e) {
            logger.error("Error creating submission: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/grade")
    public ResponseEntity<?> gradeSubmission(@RequestBody GradeDTO gradeDTO,
                                             @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить teacherId из токена (временная заглушка)
            Long teacherId = 2L; // Заглушка для teacher ID

            submissionService.gradeSubmission(gradeDTO, teacherId);
            return ResponseEntity.ok(Map.of("message", "Assignment graded successfully"));
        } catch (Exception e) {
            logger.error("Error grading submission: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMySubmissions(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить userId и роль из токена (временная заглушка)
            Long userId = 3L; // Заглушка
            String userRole = "student"; // Заглушка

            List<SubmissionDTO> submissions;
            if ("student".equals(userRole)) {
                submissions = submissionService.getSubmissionsByStudent(userId);
            } else if ("teacher".equals(userRole)) {
                // TODO: Получить assignmentIds учителя из assignment-service
                List<Long> assignmentIds = List.of(1L, 2L); // Заглушка
                submissions = submissionService.getSubmissionsForTeacher(userId, assignmentIds);
            } else {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            logger.error("Error getting submissions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        try {
            List<SubmissionDTO> submissions = submissionService.getSubmissionsByAssignment(assignmentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            logger.error("Error getting submissions by assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<?> getSubmissionById(@PathVariable Long submissionId) {
        try {
            SubmissionDTO submission = submissionService.getSubmissionById(submissionId);
            return submission != null ? ResponseEntity.ok(submission) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting submission: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getSubmissionsByStatus(@PathVariable String status) {
        try {
            List<SubmissionDTO> submissions = submissionService.getSubmissionsByStatus(status);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            logger.error("Error getting submissions by status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}/stats")
    public ResponseEntity<?> getStudentStats(@PathVariable Long studentId) {
        try {
            Map<String, Long> stats = submissionService.getSubmissionStats(studentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting student stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}