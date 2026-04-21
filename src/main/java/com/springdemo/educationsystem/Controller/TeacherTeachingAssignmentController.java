package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.TeacherClassSubjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/teaching-assignments")
@CrossOrigin("*")
public class TeacherTeachingAssignmentController {

    private final TeacherClassSubjectService teacherClassSubjectService;
    private final AuthService authService;

    public TeacherTeachingAssignmentController(TeacherClassSubjectService teacherClassSubjectService,
                                               AuthService authService) {
        this.teacherClassSubjectService = teacherClassSubjectService;
        this.authService = authService;
    }

    @GetMapping("/pairs")
    public ResponseEntity<?> getMyPairs(@RequestHeader("Authorization") String authorizationHeader) {
        Long userId = requireTeacher(authorizationHeader);
        return ResponseEntity.ok(teacherClassSubjectService.getTeacherPairs(userId));
    }

    @GetMapping("/classes")
    public ResponseEntity<?> getMyClasses(@RequestHeader("Authorization") String authorizationHeader) {
        Long userId = requireTeacher(authorizationHeader);
        return ResponseEntity.ok(teacherClassSubjectService.getTeacherClasses(userId));
    }

    @GetMapping("/subjects")
    public ResponseEntity<?> getMySubjects(@RequestHeader("Authorization") String authorizationHeader) {
        Long userId = requireTeacher(authorizationHeader);
        return ResponseEntity.ok(teacherClassSubjectService.getTeacherSubjects(userId));
    }

    private Long requireTeacher(String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        if (token.isBlank() || !authService.isValidToken(token)) {
            throw new RuntimeException("Authentication required");
        }

        String role = authService.getUserRole(token);
        if (!"teacher".equals(role)) {
            throw new RuntimeException("Only teachers can access teaching assignments");
        }

        return authService.getUserId(token);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return "";
    }
}