package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.Entity.Subject;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.SubjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin("*")
public class SubjectController {

    private final SubjectService subjectService;
    private final AuthService authService;

    public SubjectController(SubjectService subjectService, AuthService authService) {
        this.subjectService = subjectService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getAllSubjects(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        // Проверка авторизации (опционально, можно убрать если нужно публичный доступ)
        if (authorizationHeader != null) {
            String token = extractToken(authorizationHeader);
            if (!authService.isValidToken(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }
        }

        try {
            List<Subject> subjects = subjectService.getAllSubjects();
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }
}