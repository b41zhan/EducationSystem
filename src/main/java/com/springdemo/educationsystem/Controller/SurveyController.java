package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.*;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.SurveyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/surveys")
@CrossOrigin("*")
public class SurveyController {

    private final SurveyService surveyService;
    private final AuthService authService;

    public SurveyController(SurveyService surveyService, AuthService authService) {
        this.surveyService = surveyService;
        this.authService = authService;
    }

    // ====== ADMIN ======

    @PostMapping
    public ResponseEntity<?> createSurvey(@RequestBody SurveyCreateDTO dto,
                                          @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token) || !authService.isAdmin(token)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied. Admin rights required."));
        }

        Long adminId = authService.getUserId(token);
        surveyService.createSurvey(dto, adminId);
        return ResponseEntity.ok(Map.of("message", "Survey created"));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<SurveyShortDTO>> getAllSurveysAdmin(
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token) || !authService.isAdmin(token)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(surveyService.getAllSurveysForAdmin());
    }

    @GetMapping("/{surveyId}/results")
    public ResponseEntity<?> getSurveyResults(@PathVariable Long surveyId,
                                              @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token) || !authService.isAdmin(token)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied. Admin rights required."));
        }
        return ResponseEntity.ok(surveyService.getResults(surveyId));
    }

    // ====== STUDENT / TEACHER ======

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableSurveys(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long userId = authService.getUserId(token);
        String role = authService.getUserRole(token);

        return ResponseEntity.ok(surveyService.getAvailableSurveys(userId, role));
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurveyDetails(@PathVariable Long surveyId,
                                              @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        return ResponseEntity.ok(surveyService.getSurveyDetails(surveyId));
    }

    @PostMapping("/answer")
    public ResponseEntity<?> submitSurvey(@RequestBody SurveyResponseRequest request,
                                          @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long userId = authService.getUserId(token);
        surveyService.saveResponse(userId, request);

        return ResponseEntity.ok(Map.of("message", "Ответы сохранены"));
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : authorizationHeader;
    }
}
