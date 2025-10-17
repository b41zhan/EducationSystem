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
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
//        if (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) {
//            String token = generateToken();
//            UserInfo userInfo = new UserInfo(0L, email, "admin");
//            activeTokens.put(token, userInfo);
//
//            logger.info("Admin login successful, token: {}", token);
//            return createLoginResponse(token, 0L, email, "Системный", "Администратор", "admin");
//        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getPasswordHash().equals(password)) {
            String token = generateToken();

            String role = determineUserRole(user);

            UserInfo userInfo = new UserInfo(user.getId(), email, role);
            activeTokens.put(token, userInfo);

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