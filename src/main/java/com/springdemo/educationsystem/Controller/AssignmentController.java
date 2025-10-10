package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.AssignmentDTO;
import com.springdemo.educationsystem.Service.AssignmentService;
import com.springdemo.educationsystem.Service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final AuthService authService;
    public AssignmentController(AssignmentService assignmentService, AuthService authService) {
        this.assignmentService = assignmentService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getAllAssignments(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        logger.info("Getting all assignments");

        if (!isAuthenticated(authorizationHeader)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            List<AssignmentDTO> assignments = assignmentService.getAllAssignments();
            logger.info("Found {} assignments", assignments.size());
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting assignments: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAssignmentById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        logger.info("Getting assignment by id: {}", id);

        // Проверяем авторизацию
        if (!isAuthenticated(authorizationHeader)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            AssignmentDTO assignment = assignmentService.getAssignmentById(id);
            if (assignment != null) {
                return ResponseEntity.ok(assignment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-class")
    public ResponseEntity<?> getAssignmentsForMyClass(@RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.status(404).body(Map.of("error", "Use /api/students/assignments/my-class endpoint"));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getAssignmentsByClass(
            @PathVariable Long classId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        logger.info("Getting assignments for class: {}", classId);

        if (!isAuthenticated(authorizationHeader)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByClass(classId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting assignments by class: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private boolean isAuthenticated(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authorizationHeader.substring(7);
        return authService.isValidToken(token);
    }
}
