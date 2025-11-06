package nba.studix.userservice.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin("*")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @PostMapping("/validate-simple")
    public ResponseEntity<?> validateSimple(@RequestBody Map<String, String> credentials) {
        try {
            logger.info("=== SIMPLE VALIDATION CALLED ===");
            logger.info("Email: {}", credentials.get("email"));
            logger.info("Password: {}", credentials.get("password"));

            // Простая проверка для админа
            if ("admin@educationsystem.com".equals(credentials.get("email")) &&
                    "admin123".equals(credentials.get("password"))) {
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "userId", 1
                ));
            }

            return ResponseEntity.status(401).body(Map.of(
                    "valid", false,
                    "error", "Invalid credentials"
            ));

        } catch (Exception e) {
            logger.error("Error in simple validation: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "valid", false,
                    "error", "Internal server error"
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is UP and RUNNING");
    }
}