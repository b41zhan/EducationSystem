package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.WikipediaDTO;
import com.springdemo.educationsystem.Service.WikipediaService;
import com.springdemo.educationsystem.Service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wikipedia")
@CrossOrigin("*")
public class WikipediaController {

    private static final Logger logger = LoggerFactory.getLogger(WikipediaController.class);

    private final WikipediaService wikipediaService;
    private final AuthService authService;

    public WikipediaController(WikipediaService wikipediaService, AuthService authService) {
        this.wikipediaService = wikipediaService;
        this.authService = authService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchWikipedia(
            @RequestParam String query,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        logger.info("🔍 Wikipedia search request: {}", query);

        // Проверяем авторизацию
        if (!isAuthenticated(authorizationHeader)) {
            logger.warn("❌ Unauthorized Wikipedia search attempt");
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query cannot be empty"));
        }

        try {
            List<WikipediaDTO> results = wikipediaService.search(query.trim());
            logger.info("✅ Search completed, found {} results", results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("💥 Search failed for query '{}': {}", query, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }

    private boolean isAuthenticated(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authorizationHeader.substring(7);
        return authService.isValidToken(token);
    }
}