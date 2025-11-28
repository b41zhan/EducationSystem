package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    public AuthService(UserRepository userRepository,  GamificationService gamificationService) {
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
    }

    private Map<String, UserInfo> activeTokens = new HashMap<>();

    private final String ADMIN_EMAIL = "admin@school.kz";
    private final String ADMIN_PASSWORD = "admin";


    private static class UserInfo {
        Long userId;
        String email;
        String role;

        UserInfo(Long userId, String email, String role) {
            this.userId = userId;
            this.email = email;
            this.role = role;
        }
    }

    public Map<String, Object> login(String email, String password) {
        logger.info("Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getPasswordHash().equals(password)) {
            String token = generateToken();

            String role = determineUserRole(user);

            UserInfo userInfo = new UserInfo(user.getId(), email, role);
            activeTokens.put(token, userInfo);

            // АВТОМАТИЧЕСКАЯ ИНИЦИАЛИЗАЦИЯ СТАТИСТИКИ ДЛЯ СТУДЕНТОВ
            if ("student".equals(role)) {
                try {
                    gamificationService.initializeStudentStats(user.getId());
                    logger.info("Auto-initialized gamification stats for student: {}", user.getId());
                } catch (Exception e) {
                    logger.warn("Could not initialize gamification stats for student {}: {}", user.getId(), e.getMessage());
                }
            }

            logger.info("User login successful, email: {}, role: {}, token: {}", email, role, token);
            return createLoginResponse(token, user.getId(), user.getEmail(),
                    user.getFirstName(), user.getLastName(), role);
        }

        logger.warn("Login failed for email: {}", email);
        throw new RuntimeException("Invalid credentials");
    }

    private String determineUserRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return "unknown";
        }

        // Берем первую роль из списка (обычно у пользователя одна основная роль)
        return user.getRoles().get(0).getName();
    }

    private Map<String, Object> createLoginResponse(String token, Long userId, String email,
                                                    String firstName, String lastName, String role) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", userId);
        response.put("email", email);
        response.put("firstName", firstName);
        response.put("lastName", lastName);
        response.put("role", role);
        response.put("message", "Login successful");
        return response;
    }

    public boolean isAdmin(String token) {
        UserInfo userInfo = activeTokens.get(token);
        return userInfo != null && "admin".equals(userInfo.role);
    }

    public Long getUserId(String token) {
        UserInfo userInfo = activeTokens.get(token);
        return userInfo != null ? userInfo.userId : null;
    }

    public String getUserRole(String token) {
        UserInfo userInfo = activeTokens.get(token);
        return userInfo != null ? userInfo.role : null;
    }
    public boolean isValidToken(String token) {
        return activeTokens.containsKey(token);
    }

    public void logout(String token) {
        activeTokens.remove(token);
        logger.info("User logged out, token removed: {}", token);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}