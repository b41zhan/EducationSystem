package nba.studix.assignmentservice.Controller;

import nba.studix.assignmentservice.DTO.AssignmentDTO;
import nba.studix.assignmentservice.Service.AssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin("*")
public class AssignmentController {
    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<?> getAllAssignments() {
        try {
            logger.info("Getting all assignments");
            List<AssignmentDTO> assignments = assignmentService.getAllAssignments();
            logger.info("Found {} assignments", assignments.size());
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting assignments: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long id) {
        try {
            logger.info("Getting assignment by id: {}", id);
            AssignmentDTO assignment = assignmentService.getAssignmentById(id);
            return assignment != null ? ResponseEntity.ok(assignment) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getAssignmentsByClass(@PathVariable Long classId) {
        try {
            logger.info("Getting assignments for class: {}", classId);
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByClass(classId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting assignments by class: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getAssignmentsByTeacher(@PathVariable Long teacherId) {
        try {
            logger.info("Getting assignments for teacher: {}", teacherId);
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByTeacher(teacherId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting assignments by teacher: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<?> getAssignmentsBySubject(@PathVariable Long subjectId) {
        try {
            logger.info("Getting assignments for subject: {}", subjectId);
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsBySubject(subjectId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting assignments by subject: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getAssignmentsByType(@PathVariable String type) {
        try {
            logger.info("Getting assignments of type: {}", type);
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByType(type);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting assignments by type: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueAssignments() {
        try {
            logger.info("Getting overdue assignments");
            List<AssignmentDTO> assignments = assignmentService.getOverdueAssignments();
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting overdue assignments: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}