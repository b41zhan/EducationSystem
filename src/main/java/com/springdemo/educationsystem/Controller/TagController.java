package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.TagDTO;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin("*")
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    private final TagService tagService;
    private final AuthService authService;

    public TagController(TagService tagService, AuthService authService) {
        this.tagService = tagService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getAllTags(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            List<TagDTO> tags = tagService.getAllTags();
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            logger.error("Error getting tags: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/my")
    public ResponseEntity<?> getMyTags(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long studentId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"student".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only students can access tags"));
        }

        try {
            List<TagDTO> tags = tagService.getStudentTags(studentId);
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            logger.error("Error getting student tags: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/available")
    public ResponseEntity<?> getAvailableTags(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long studentId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"student".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only students can access tags"));
        }

        try {
            List<TagDTO> tags = tagService.getAvailableTagsForStudent(studentId);
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            logger.error("Error getting available tags: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/student/update")
    public ResponseEntity<?> updateStudentTags(
            @RequestBody Map<String, List<Long>> request,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Long studentId = authService.getUserId(token);
        String userRole = authService.getUserRole(token);

        if (!"student".equals(userRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only students can update tags"));
        }

        List<Long> tagIds = request.get("tagIds");

        try {
            tagService.updateStudentTags(studentId, tagIds);
            logger.info("Student {} updated tags: {}", studentId, tagIds);
            return ResponseEntity.ok(Map.of("message", "Теги успешно обновлены"));
        } catch (Exception e) {
            logger.error("Error updating student tags: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }
}