package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.ClassStatsDTO;
import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin("*")
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    private final StatisticsService statisticsService;
    private final AuthService authService;

    public StatisticsController(StatisticsService statisticsService, AuthService authService) {
        this.statisticsService = statisticsService;
        this.authService = authService;
    }

    @GetMapping("/teacher/classes")
    public ResponseEntity<?> getTeacherClasses(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long teacherId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"teacher".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers can access statistics"));
        }

        try {
            List<SchoolClass> classes = statisticsService.getTeacherClasses(teacherId);
            logger.info("Retrieved {} classes for teacher {}", classes.size(), teacherId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            logger.error("Error getting teacher classes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getClassStatistics(
            @PathVariable Long classId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long teacherId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"teacher".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers can access statistics"));
        }

        try {
            ClassStatsDTO classStats = statisticsService.getClassStatistics(teacherId, classId);
            logger.info("Retrieved statistics for class {} by teacher {}", classId, teacherId);
            return ResponseEntity.ok(classStats);
        } catch (Exception e) {
            logger.error("Error getting class statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/teacher/summary")
    public ResponseEntity<?> getTeacherStatisticsSummary(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long teacherId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"teacher".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers can access statistics"));
        }

        try {
            List<ClassStatsDTO> classesStats = statisticsService.getTeacherClassesStatistics(teacherId);
            logger.info("Retrieved summary statistics for teacher {}", teacherId);
            return ResponseEntity.ok(classesStats);
        } catch (Exception e) {
            logger.error("Error getting teacher statistics summary: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
}
