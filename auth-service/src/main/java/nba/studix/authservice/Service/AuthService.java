package nba.studix.authservice.Service;

import nba.studix.authservice.Client.UserServiceClient;
import nba.studix.authservice.DTO.LoginRequestDTO;
import nba.studix.authservice.DTO.LoginResponseDTO;
import nba.studix.authservice.DTO.TokenValidationResponseDTO;
import nba.studix.authservice.DTO.UserInfoDTO;
import nba.studix.authservice.Entity.ActiveToken;
import nba.studix.authservice.Repository.ActiveTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final ActiveTokenRepository activeTokenRepository;
    private final UserServiceClient userServiceClient;

    @Value("${app.auth.token-expiration-hours:24}")
    private int tokenExpirationHours;

    public AuthService(ActiveTokenRepository activeTokenRepository, UserServiceClient userServiceClient) {
        this.activeTokenRepository = activeTokenRepository;
        this.userServiceClient = userServiceClient;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            // Подготавливаем данные для user-service
            Map<String, String> credentials = new HashMap<>();
            credentials.put("email", loginRequest.getEmail());
            credentials.put("password", loginRequest.getPassword());

            // РЕАЛЬНАЯ ПРОВЕРКА через user-service
            ResponseEntity<Map<String, Object>> validationResponse = userServiceClient.validateCredentials(credentials);

            logger.info("Validation response status: {}", validationResponse.getStatusCode());

            if (validationResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Authentication failed with status: " + validationResponse.getStatusCode());
            }

            Map<String, Object> validationResult = validationResponse.getBody();

            if (validationResult == null) {
                throw new RuntimeException("Empty response from user service");
            }

            Boolean isValid = (Boolean) validationResult.get("valid");
            if (!Boolean.TRUE.equals(isValid)) {
                String error = (String) validationResult.get("error");
                throw new RuntimeException(error != null ? error : "Invalid credentials");
            }

            Long userId = ((Number) validationResult.get("userId")).longValue();

            // РЕАЛЬНОЕ ПОЛУЧЕНИЕ ИНФОРМАЦИИ О ПОЛЬЗОВАТЕЛЕ
            ResponseEntity<UserInfoDTO> userInfoResponse = userServiceClient.getUserInfoByEmail(loginRequest.getEmail());

            if (userInfoResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to get user info: " + userInfoResponse.getStatusCode());
            }

            UserInfoDTO userInfo = userInfoResponse.getBody();

            if (userInfo == null) {
                throw new RuntimeException("Empty user info response");
            }

            String primaryRole = userInfo.getRoles().isEmpty() ? "USER" : userInfo.getRoles().get(0);

            // Создание токена
            String token = generateToken();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpirationHours);

            ActiveToken activeToken = new ActiveToken(token, userId, loginRequest.getEmail(), primaryRole, expiresAt);
            activeTokenRepository.save(activeToken);

            logger.info("User login successful, email: {}, role: {}, token: {}",
                    loginRequest.getEmail(), primaryRole, token);

            return createLoginResponse(token, userId, loginRequest.getEmail(),
                    userInfo.getFirstName(), userInfo.getLastName(), primaryRole,
                    "Login successful", userInfo.getRoles());

        } catch (Exception e) {
            logger.error("Login failed for email: {}", loginRequest.getEmail(), e);
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    public TokenValidationResponseDTO validateToken(String token) {
        logger.info("Validating token: {}", token);

        cleanExpiredTokens();

        Optional<ActiveToken> activeTokenOpt = activeTokenRepository.findByToken(token);
        if (activeTokenOpt.isEmpty()) {
            return new TokenValidationResponseDTO("Invalid token");
        }

        ActiveToken activeToken = activeTokenOpt.get();

        if (activeToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            activeTokenRepository.delete(activeToken);
            return new TokenValidationResponseDTO("Token expired");
        }

        return new TokenValidationResponseDTO(true, activeToken.getUserRole(), activeToken.getUserId());
    }

    public void logout(String token) {
        activeTokenRepository.findByToken(token).ifPresent(activeTokenRepository::delete);
        logger.info("User logged out, token removed: {}", token);
    }

    @Transactional
    public void cleanExpiredTokens() {
        activeTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private LoginResponseDTO createLoginResponse(String token, Long userId, String email,
                                                 String firstName, String lastName, String role,
                                                 String message, List<String> roles) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUserId(userId);
        response.setEmail(email);
        response.setFirstName(firstName);
        response.setLastName(lastName);
        response.setRole(role);
        response.setMessage(message);
        response.setRoles(roles);
        return response;
    }
}