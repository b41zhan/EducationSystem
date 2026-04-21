package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.AdminTeacherClassSubjectCreateDTO;
import com.springdemo.educationsystem.Entity.Role;
import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Repository.UserRepository;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.TeacherClassSubjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/teaching-assignments")
@CrossOrigin("*")
public class AdminTeacherClassSubjectController {

    private final TeacherClassSubjectService teacherClassSubjectService;
    private final AuthService authService;
    private final UserRepository userRepository;

    public AdminTeacherClassSubjectController(TeacherClassSubjectService teacherClassSubjectService,
                                              AuthService authService,
                                              UserRepository userRepository) {
        this.teacherClassSubjectService = teacherClassSubjectService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<?> getBySchool(@PathVariable Long schoolId,
                                         @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(teacherClassSubjectService.getAssignmentsBySchool(schoolId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/school/{schoolId}/teachers")
    public ResponseEntity<?> getTeachersBySchool(@PathVariable Long schoolId,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(teacherClassSubjectService.getTeachersBySchool(schoolId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AdminTeacherClassSubjectCreateDTO dto,
                                    @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(teacherClassSubjectService.createAssignment(dto));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<?> deactivate(@PathVariable Long assignmentId,
                                        @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            teacherClassSubjectService.deactivateAssignment(assignmentId);
            return ResponseEntity.ok(Map.of("message", "Teaching assignment deactivated"));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    private boolean isAdmin(String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        if (token.isBlank() || !authService.isValidToken(token)) {
            return false;
        }

        Long userId = authService.getUserId(token);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || user.getRoles() == null) {
            return false;
        }

        return user.getRoles()
                .stream()
                .map(Role::getName)
                .anyMatch(role -> "admin".equalsIgnoreCase(role));
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return "";
    }

    private ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin rights required."));
    }

    private ResponseEntity<Map<String, String>> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}