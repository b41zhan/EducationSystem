package nba.studix.assignmentservice.Controller;

import nba.studix.assignmentservice.DTO.AssignmentDTO;
import nba.studix.assignmentservice.DTO.CreateAssignmentDTO;
import nba.studix.assignmentservice.Entity.Assignment;
import nba.studix.assignmentservice.Service.AssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/assignments")
@CrossOrigin("*")
public class TeacherAssignmentController {
    private static final Logger logger = LoggerFactory.getLogger(TeacherAssignmentController.class);

    private final AssignmentService assignmentService;

    public TeacherAssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody CreateAssignmentDTO createDTO,
                                              @RequestHeader("Authorization") String authorizationHeader) {
        try {
            logger.info("Creating assignment: {}", createDTO.getTitle());

            // TODO: Получить teacherId из токена (временная заглушка)
            Long teacherId = 2L; // Заглушка для teacher ID

            Assignment createdAssignment = assignmentService.createAssignmentWithDTO(createDTO, teacherId);
            AssignmentDTO assignmentDTO = assignmentService.convertToDTO(createdAssignment);

            return ResponseEntity.ok(assignmentDTO);
        } catch (Exception e) {
            logger.error("Error creating assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyAssignments(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить teacherId из токена (временная заглушка)
            Long teacherId = 2L; // Заглушка для teacher ID

            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByTeacher(teacherId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting teacher assignments: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssignment(@PathVariable Long id,
                                              @RequestBody Assignment assignmentDetails,
                                              @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Добавить проверку владения заданием

            Assignment updatedAssignment = assignmentService.updateAssignment(id, assignmentDetails);
            if (updatedAssignment != null) {
                AssignmentDTO assignmentDTO = assignmentService.convertToDTO(updatedAssignment);
                return ResponseEntity.ok(assignmentDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id,
                                              @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Добавить проверку владения заданием

            assignmentService.deleteAssignment(id);
            return ResponseEntity.ok(Map.of("message", "Assignment deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}