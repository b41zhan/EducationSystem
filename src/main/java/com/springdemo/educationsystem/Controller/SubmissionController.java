package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.SubmissionDTO;
import com.springdemo.educationsystem.DTO.GradeDTO;
import com.springdemo.educationsystem.Service.SubmissionService;
import com.springdemo.educationsystem.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin("*")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final AuthService authService;
    public SubmissionController(SubmissionService submissionService, AuthService authService) {
        this.submissionService = submissionService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<?> createSubmission(
            @RequestBody Map<String, Object> submissionData,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long studentId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"student".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only students can submit assignments"));
        }

        try {
            Long assignmentId = Long.valueOf(submissionData.get("assignmentId").toString());
            String filePath = (String) submissionData.get("filePath");
            String fileName = (String) submissionData.get("fileName");
            Long fileSize = Long.valueOf(submissionData.get("fileSize").toString());
            String comment = (String) submissionData.get("comment");

            submissionService.createSubmission(assignmentId, studentId, filePath, fileName, fileSize, comment);

            return ResponseEntity.ok(Map.of("message", "Assignment submitted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/grade")
    public ResponseEntity<?> gradeSubmission(
            @RequestBody GradeDTO gradeDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long teacherId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"teacher".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers can grade assignments"));
        }

        try {
            submissionService.gradeSubmission(gradeDTO, teacherId);
            return ResponseEntity.ok(Map.of("message", "Assignment graded successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMySubmissions(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long userId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        try {
            List<SubmissionDTO> submissions;
            if ("student".equals(userRole)) {
                submissions = submissionService.getSubmissionsByStudent(userId);
            } else if ("teacher".equals(userRole)) {
                submissions = submissionService.getSubmissionsForTeacher(userId);
            } else {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            return ResponseEntity.ok(submissions);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getSubmissionsByAssignment(
            @PathVariable Long assignmentId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            List<SubmissionDTO> submissions = submissionService.getSubmissionsByAssignment(assignmentId);
            return ResponseEntity.ok(submissions);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
}