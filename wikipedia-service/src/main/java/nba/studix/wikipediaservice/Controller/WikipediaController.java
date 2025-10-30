package nba.studix.wikipediaservice.Controller;

import nba.studix.wikipediaservice.DTO.WikipediaDTO;
import nba.studix.wikipediaservice.DTO.SearchRequestDTO;
import nba.studix.wikipediaservice.DTO.SearchResponseDTO;
import nba.studix.wikipediaservice.Service.WikipediaService;
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

    public WikipediaController(WikipediaService wikipediaService) {
        this.wikipediaService = wikipediaService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchWikipedia(@RequestParam String query,
                                             @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            logger.info("🔍 Wikipedia search request: {}", query);

            // TODO: Добавить проверку аутентификации если нужно
            // if (!isAuthenticated(authorizationHeader)) {
            //     return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            // }

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Query cannot be empty"));
            }

            List<WikipediaDTO> results = wikipediaService.search(query.trim());
            logger.info("✅ Search completed, found {} results", results.size());

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("💥 Search failed for query '{}': {}", query, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchWikipediaPost(@RequestBody SearchRequestDTO searchRequest,
                                                 @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            logger.info("🔍 Wikipedia search request (POST): {}", searchRequest.getQuery());

            if (searchRequest.getQuery() == null || searchRequest.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Query cannot be empty"));
            }

            SearchResponseDTO response = wikipediaService.searchWithMetadata(searchRequest.getQuery().trim());
            logger.info("✅ Search completed, found {} results, from cache: {}",
                    response.getTotalResults(), response.getFromCache());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("💥 Search failed for query '{}': {}", searchRequest.getQuery(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            // Простой тестовый запрос для проверки работы сервиса
            List<WikipediaDTO> results = wikipediaService.search("математика");
            return ResponseEntity.ok(Map.of(
                    "status", "healthy",
                    "service", "wikipedia-service",
                    "testSearchResults", results.size()
            ));
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                    "status", "unhealthy",
                    "service", "wikipedia-service",
                    "error", e.getMessage()
            ));
        }
    }

    // Вспомогательный метод для проверки аутентификации
    private boolean isAuthenticated(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }
        // TODO: Реализовать проверку токена через auth-service
        return true;
    }
}