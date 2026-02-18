package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.AssignmentDTO;
import com.springdemo.educationsystem.DTO.StudentGradeDTO;
import com.springdemo.educationsystem.Entity.Grade;
import com.springdemo.educationsystem.Entity.Student;
import com.springdemo.educationsystem.Repository.GradeRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@CrossOrigin("*")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final StudentRepository studentRepository;
    private final AssignmentService assignmentService;
    private final AuthService authService;
    private final GradeRepository gradeRepository;
    public StudentController(StudentRepository studentRepository, AssignmentService assignmentService, AuthService authService, GradeRepository gradeRepository) {
        this.studentRepository = studentRepository;
        this.assignmentService = assignmentService;
        this.authService = authService;
        this.gradeRepository = gradeRepository;
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

//    private String extractToken(String authorizationHeader) {
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            return authorizationHeader.substring(7);
//        }
//        return "";
//    }

    @GetMapping("/grades")
    public ResponseEntity<?> getMyGrades(
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String token = extractToken(authorizationHeader);
            if (!authService.isValidToken(token)) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication required"));
            }

            Long userId = authService.getUserId(token);

            // StudentRepository уже используется в других контроллерах через findByUserId(...)
            Student student = studentRepository.findByUserId(userId).orElse(null);

            if (student == null) {
                logger.info("Student not found for user {}", userId);
                return ResponseEntity.ok(List.of());
            }

            List<Grade> grades = gradeRepository.findByStudentId(student.getId());
            logger.info("Loaded {} grades for student {}", grades.size(), student.getId());

            List<StudentGradeDTO> response = grades.stream()
                    .map(this::convertToStudentGradeDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error while loading student grades: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    private StudentGradeDTO convertToStudentGradeDTO(Grade grade) {

        String assignmentTitle = null;
        String subjectName = null;

        if (grade.getSubmission() != null &&
                grade.getSubmission().getAssignment() != null) {

            assignmentTitle = grade.getSubmission().getAssignment().getTitle();

            if (grade.getSubmission().getAssignment().getSubject() != null) {
                subjectName = grade.getSubmission().getAssignment().getSubject().getName();
            }
        }

        return new StudentGradeDTO(
                assignmentTitle,
                subjectName,
                grade.getGradeValue(),
                grade.getGradedAt(),
                grade.getComment()
        );
    }


    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return "";
    }

    @GetMapping("/classes/{classId}/students")
    public List<StudentDTO> getStudentsByClass(@PathVariable Long classId) {
        return studentRepository.findBySchoolClass_Id(classId)
                .stream()
                .map(s -> new StudentDTO(
                        s.getId(),
                        s.getUser().getFirstName(),
                        s.getUser().getLastName(),
                        s.getUser().getPatronymic(),
                        s.getSchoolClass().getName()
                ))
                .toList();
    }

    public record StudentDTO(Long id, String firstName, String lastName, String patronymic, String className) {}

}