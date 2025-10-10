package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.AssignmentDTO;
import com.springdemo.educationsystem.Entity.Student;
import com.springdemo.educationsystem.Repository.StudentRepository;
import com.springdemo.educationsystem.Service.AssignmentService;
import com.springdemo.educationsystem.Service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin("*")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final StudentRepository studentRepository;
    private final AssignmentService assignmentService;
    private final AuthService authService;
    public StudentController(StudentRepository studentRepository, AssignmentService assignmentService, AuthService authService) {
        this.studentRepository = studentRepository;
        this.assignmentService = assignmentService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentStudent(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            if (!authService.isValidToken(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Long userId = authService.getUserId(token);
            Student student = studentRepository.findById(userId).orElse(null);

            if (student == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Student not found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", student.getId());
            response.put("userId", student.getUser().getId());

            if (student.getSchoolClass() != null) {
                Map<String, Object> classInfo = new HashMap<>();
                classInfo.put("id", student.getSchoolClass().getId());
                classInfo.put("name", student.getSchoolClass().getName());
                classInfo.put("academicYear", student.getSchoolClass().getAcademicYear());
                response.put("schoolClass", classInfo);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/assignments/my-class")
    public ResponseEntity<?> getAssignmentsForMyClass(
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String token = extractToken(authorizationHeader);
            if (!authService.isValidToken(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Long userId = authService.getUserId(token);
            Student student = studentRepository.findById(userId).orElse(null);

            if (student == null || student.getSchoolClass() == null) {
                logger.info("Student not found or no class assigned for user: {}", userId);
                return ResponseEntity.ok(List.of());
            }

            Long classId = student.getSchoolClass().getId();
            logger.info("Loading assignments for class: {}", classId);

            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByClass(classId);
            logger.info("Found {} assignments", assignments.size());

            return ResponseEntity.ok(assignments);

        } catch (Exception e) {
            logger.error("Error getting assignments for student class: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return "";
    }

}