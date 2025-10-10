package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.AssignmentDTO;
import com.springdemo.educationsystem.DTO.CreateAssignmentDTO;
import com.springdemo.educationsystem.Entity.Assignment;
import com.springdemo.educationsystem.Service.AssignmentService;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final AuthService authService;
    private final TeacherService teacherService;
    public TeacherAssignmentController(AssignmentService assignmentService, AuthService authService,TeacherService teacherService) {
        this.assignmentService = assignmentService;
        this.authService = authService;
        this.teacherService = teacherService;
    }


    @PostMapping
    public ResponseEntity<?> createAssignment(
            @RequestBody CreateAssignmentDTO createDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        logger.info("Creating assignment: {}", createDTO.getTitle());

        if (!isTeacher(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers can create assignments"));
        }

        Long teacherId = getCurrentTeacherId(authorizationHeader);

        try {
            Assignment createdAssignment = assignmentService.createAssignmentWithDTO(createDTO, teacherId);
            AssignmentDTO assignmentDTO = assignmentService.convertToDTO(createdAssignment);
            return ResponseEntity.ok(assignmentDTO);
        } catch (Exception e) {
            logger.error("Error creating assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyAssignments(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!isTeacher(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        Long teacherId = getCurrentTeacherId(authorizationHeader);
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByTeacher(teacherId);

        return ResponseEntity.ok(assignments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long id,
            @RequestBody Assignment assignmentDetails,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!isTeacher(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        if (!isAssignmentOwner(id, authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only edit your own assignments"));
        }

        Assignment updatedAssignment = assignmentService.updateAssignment(id, assignmentDetails);
        if (updatedAssignment != null) {
            AssignmentDTO assignmentDTO = assignmentService.convertToDTO(updatedAssignment);
            return ResponseEntity.ok(assignmentDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!isTeacher(authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        if (!isAssignmentOwner(id, authorizationHeader)) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only delete your own assignments"));
        }

        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok(Map.of("message", "Assignment deleted successfully"));
    }

    private boolean isTeacher(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        String role = authService.getUserRole(token);
        return "teacher".equals(role);
    }

    private Long getCurrentTeacherId(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        return authService.getUserId(token);
    }

    private boolean isAssignmentOwner(Long assignmentId, String authorizationHeader) {
        Long currentTeacherId = getCurrentTeacherId(authorizationHeader);
        AssignmentDTO assignment = assignmentService.getAssignmentById(assignmentId);
        return assignment != null && assignment.getTeacherId().equals(currentTeacherId);
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
}