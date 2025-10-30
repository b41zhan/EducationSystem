package nba.studix.authservice.Controller;

import nba.studix.authservice.DTO.LoginRequest;
import nba.studix.authservice.DTO.LoginResponse;
import nba.studix.authservice.DTO.TokenValidationResponse;
import nba.studix.authservice.Service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Received login request for email: {}", loginRequest.getEmail());

            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Login failed", "message", e.getMessage())
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        try {
            String token = authorizationHeader.substring(7);
            TokenValidationResponse validationResponse = authService.validateToken(token);
            return ResponseEntity.ok(validationResponse);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            String token = authorizationHeader.substring(7);

            if (!authService.isValidToken(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
            }

            Long userId = authService.getUserId(token);
            String role = authService.getUserRole(token);

            // Здесь позже добавим вызов user-service для получения полной информации
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "role", role,
                    "message", "User info will be fetched from user-service"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}