package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.TeacherJournalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/journal/student")
@CrossOrigin("*")
public class StudentJournalController {

    private final TeacherJournalService teacherJournalService;
    private final AuthService authService;

    public StudentJournalController(TeacherJournalService teacherJournalService, AuthService authService) {
        this.teacherJournalService = teacherJournalService;
        this.authService = authService;
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyJournal(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Integer quarter
    ) {
        String token = authorizationHeader.substring(7);

        if (!authService.isValidToken(token)) {
            throw new RuntimeException("Authentication required");
        }

        String role = authService.getUserRole(token);
        if (!"student".equals(role)) {
            throw new RuntimeException("Only students can access this journal");
        }

        Long userId = authService.getUserId(token);
        return ResponseEntity.ok(teacherJournalService.getStudentJournal(userId, quarter));
    }
}