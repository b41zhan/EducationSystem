package nba.studix.submissionservice.Controller;

import nba.studix.submissionservice.DTO.SubmissionDTO;
import nba.studix.submissionservice.Service.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin("*")
public class StudentController {
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final SubmissionService submissionService;

    public StudentController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentStudent(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить studentId из токена (временная заглушка)
            Long studentId = 3L; // Заглушка

            // TODO: Получить информацию о студенте из user-service
            Map<String, Object> response = Map.of(
                    "id", studentId,
                    "userId", studentId,
                    "message", "Student info will be fetched from user-service"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting student info: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/assignments/my-class")
    public ResponseEntity<?> getAssignmentsForMyClass(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить classId студента из user-service (временная заглушка)
            Long classId = 1L; // Заглушка

            // TODO: Получить задания из assignment-service
            List<Object> assignments = List.of(); // Заглушка

            logger.info("Loading assignments for class: {}", classId);
            logger.info("Found {} assignments", assignments.size());

            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting assignments for student class: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{studentId}/submissions")
    public ResponseEntity<?> getStudentSubmissions(@PathVariable Long studentId) {
        try {
            List<SubmissionDTO> submissions = submissionService.getSubmissionsByStudent(studentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            logger.error("Error getting student submissions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{studentId}/stats")
    public ResponseEntity<?> getStudentSubmissionStats(@PathVariable Long studentId) {
        try {
            Map<String, Long> stats = submissionService.getSubmissionStats(studentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting student submission stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}