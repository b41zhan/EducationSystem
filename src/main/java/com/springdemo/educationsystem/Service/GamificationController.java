//package com.springdemo.educationsystem.Controller;
//
//import com.springdemo.educationsystem.DTO.AchievementDTO;
//import com.springdemo.educationsystem.DTO.LeaderboardDTO;
//import com.springdemo.educationsystem.DTO.StudentGamificationStatsDTO;
//import com.springdemo.educationsystem.Service.AuthService;
//import com.springdemo.educationsystem.Service.GamificationService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/gamification")
//@CrossOrigin("*")
//public class GamificationController {
//
//    private static final Logger logger = LoggerFactory.getLogger(GamificationController.class);
//
//    private final GamificationService gamificationService;
//    private final AuthService authService;
//
//    public GamificationController(GamificationService gamificationService, AuthService authService) {
//        this.gamificationService = gamificationService;
//        this.authService = authService;
//    }
//
//    @GetMapping("/student/stats")
//    public ResponseEntity<?> getStudentStats(
//            @RequestHeader("Authorization") String authorizationHeader) {
//
//        String token = extractToken(authorizationHeader);
//        if (!authService.isValidToken(token)) {
//            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
//        }
//
//        Long studentId = authService.getUserId(token);
//        String userRole = authService.getUserRole(token);
//
//        if (!"student".equals(userRole)) {
//            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Students only."));
//        }
//
//        try {
//            StudentGamificationStatsDTO stats = gamificationService.getStudentGamificationStats(studentId);
//            return ResponseEntity.ok(stats);
//        } catch (Exception e) {
//            logger.error("Error getting student gamification stats: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    @GetMapping("/teacher/student/{studentId}/stats")
//    public ResponseEntity<?> getStudentStatsForTeacher(
//            @PathVariable Long studentId,
//            @RequestHeader("Authorization") String authorizationHeader) {
//
//        String token = extractToken(authorizationHeader);
//        if (!authService.isValidToken(token)) {
//            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
//        }
//
//        String userRole = authService.getUserRole(token);
//
//        if (!"teacher".equals(userRole)) {
//            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Teachers only."));
//        }
//
//        try {
//            StudentGamificationStatsDTO stats = gamificationService.getStudentGamificationStats(studentId);
//            return ResponseEntity.ok(stats);
//        } catch (Exception e) {
//            logger.error("Error getting student gamification stats for teacher: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    @GetMapping("/leaderboard")
//    public ResponseEntity<?> getLeaderboard(
//            @RequestParam(required = false) Long classId,
//            @RequestHeader("Authorization") String authorizationHeader) {
//
//        String token = extractToken(authorizationHeader);
//        if (!authService.isValidToken(token)) {
//            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
//        }
//
//        try {
//            List<LeaderboardDTO> leaderboard = gamificationService.getLeaderboard(classId);
//            return ResponseEntity.ok(leaderboard);
//        } catch (Exception e) {
//            logger.error("Error getting leaderboard: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    @GetMapping("/student/achievements")
//    public ResponseEntity<?> getStudentAchievements(
//            @RequestHeader("Authorization") String authorizationHeader) {
//
//        String token = extractToken(authorizationHeader);
//        if (!authService.isValidToken(token)) {
//            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
//        }
//
//        Long studentId = authService.getUserId(token);
//        String userRole = authService.getUserRole(token);
//
//        if (!"student".equals(userRole)) {
//            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Students only."));
//        }
//
//        try {
//            List<AchievementDTO> achievements = gamificationService.getStudentAchievements(studentId);
//            return ResponseEntity.ok(achievements);
//        } catch (Exception e) {
//            logger.error("Error getting student achievements: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    @PostMapping("/admin/initialize-achievements")
//    public ResponseEntity<?> initializeAchievements(
//            @RequestHeader("Authorization") String authorizationHeader) {
//
//        String token = extractToken(authorizationHeader);
//        if (!authService.isValidToken(token)) {
//            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
//        }
//
//        if (!authService.isAdmin(token)) {
//            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin only."));
//        }
//
//        try {
//            gamificationService.initializeDefaultAchievements();
//            return ResponseEntity.ok(Map.of("message", "Default achievements initialized successfully"));
//        } catch (Exception e) {
//            logger.error("Error initializing achievements: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    private String extractToken(String authorizationHeader) {
//        return authorizationHeader.substring(7);
//    }
//}

package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.AchievementDTO;
import com.springdemo.educationsystem.DTO.LeaderboardDTO;
import com.springdemo.educationsystem.DTO.StudentGamificationStatsDTO;
import com.springdemo.educationsystem.DTO.XpHistoryDTO;
import com.springdemo.educationsystem.DTO.AchievementsStatsDTO;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.GamificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gamification")
@CrossOrigin("*")
public class GamificationController {

    private static final Logger logger = LoggerFactory.getLogger(GamificationController.class);

    private final GamificationService gamificationService;
    private final AuthService authService;

    public GamificationController(GamificationService gamificationService, AuthService authService) {
        this.gamificationService = gamificationService;
        this.authService = authService;
    }
    // --------------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЙ МЕТОД
    // --------------------------------------------------
    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
    // --------------------------------------------------
    // СТАТИСТИКА СТУДЕНТА (ОСНОВНАЯ КАРТОЧКА)
    // --------------------------------------------------
    @GetMapping("/student/stats")
    public ResponseEntity<?> getStudentStats(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long studentId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"student".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Students only."));
        }

        try {
            StudentGamificationStatsDTO stats = gamificationService.getStudentGamificationStats(studentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting student gamification stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    // --------------------------------------------------
    // СТАТИСТИКА КОНКРЕТНОГО СТУДЕНТА ДЛЯ УЧИТЕЛЯ
    // --------------------------------------------------
    @GetMapping("/teacher/student/{studentId}/stats")
    public ResponseEntity<?> getStudentStatsForTeacher(
            @PathVariable Long studentId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String userRole = authService.getUserRole(token);

        if (!"teacher".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Teachers only."));
        }

        try {
            // ВАЖНО: используем НОВЫЙ DTO
            var stats = gamificationService.getTeacherStudentDetails(studentId);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error getting student gamification details for teacher: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // --------------------------------------------------
    // ЛИДЕРБОРД
    // --------------------------------------------------

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam(required = false) Long classId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            List<LeaderboardDTO> leaderboard = gamificationService.getLeaderboard(classId);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            logger.error("Error getting leaderboard: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --------------------------------------------------
    // СПИСОК ДОСТИЖЕНИЙ СТУДЕНТА
    // --------------------------------------------------

    @GetMapping("/student/achievements")
    public ResponseEntity<?> getStudentAchievements(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long studentId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"student".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Students only."));
        }

        try {
            List<AchievementDTO> achievements = gamificationService.getStudentAchievements(studentId);
            return ResponseEntity.ok(achievements);
        } catch (Exception e) {
            logger.error("Error getting student achievements: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    // --------------------------------------------------
    // ИНИЦИАЛИЗАЦИЯ НАБОРА ДОСТИЖЕНИЙ (АДМИН)
    // --------------------------------------------------
    @PostMapping("/admin/initialize-achievements")
    public ResponseEntity<?> initializeAchievements(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        if (!authService.isAdmin(token)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin only."));
        }

        try {
            gamificationService.initializeDefaultAchievements();
            return ResponseEntity.ok(Map.of("message", "Default achievements initialized successfully"));
        } catch (Exception e) {
            logger.error("Error initializing achievements: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    // --------------------------------------------------
    // НОВОЕ: ИСТОРИЯ XP ДЛЯ ГРАФИКА
    // --------------------------------------------------
    @GetMapping("/student/xp-history")
    public ResponseEntity<?> getStudentXpHistory(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long studentId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"student".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Students only."));
        }

        try {
            List<XpHistoryDTO> history = gamificationService.getXpHistory(studentId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error getting XP history: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    // --------------------------------------------------
    // НОВОЕ: СТАТИСТИКА ДОСТИЖЕНИЙ ДЛЯ ДИАГРАММ
    // --------------------------------------------------
    @GetMapping("/student/achievement-stats")
    public ResponseEntity<?> getStudentAchievementStats(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long studentId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"student".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Students only."));
        }

        try {
            AchievementsStatsDTO stats = gamificationService.getAchievementStats(studentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting achievement stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
