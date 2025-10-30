package nba.studix.authservice.Service;

import nba.studix.authservice.DTO.LoginRequest;
import nba.studix.authservice.DTO.LoginResponse;
import nba.studix.authservice.DTO.TokenValidationResponse;
import nba.studix.authservice.Entity.ActiveToken;
import nba.studix.authservice.Entity.User;
import nba.studix.authservice.Entity.UserRole;
import nba.studix.authservice.Repository.ActiveTokenRepository;
import nba.studix.authservice.Repository.UserRepository;
import nba.studix.authservice.Repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ActiveTokenRepository activeTokenRepository;

    @Value("${app.auth.token-expiration-hours:24}")
    private int tokenExpirationHours;

    // Демо пользователи (временное решение)
    private final String ADMIN_EMAIL = "admin@school.kz";
    private final String ADMIN_PASSWORD = "admin";
    private final String TEACHER_EMAIL = "teacher.m@school.kz";
    private final String TEACHER_PASSWORD = "teacher123";
    private final String STUDENT_EMAIL = "student@school.kz";
    private final String STUDENT_PASSWORD = "password";

    public AuthService(UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       ActiveTokenRepository activeTokenRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.activeTokenRepository = activeTokenRepository;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // Проверка демо пользователей
        if (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) {
            return handleDemoUserLogin(email, "admin", "Системный", "Администратор");
        }
        if (TEACHER_EMAIL.equals(email) && TEACHER_PASSWORD.equals(password)) {
            return handleDemoUserLogin(email, "teacher", "Учитель", "Математики");
        }
        if (STUDENT_EMAIL.equals(email) && STUDENT_PASSWORD.equals(password)) {
            return handleDemoUserLogin(email, "student", "Студент", "Примеров");
        }

        // Поиск в базе данных
        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Проверка пароля (пока простое сравнение, позже добавим шифрование)
        if (!user.getPasswordHash().equals(password)) {
            throw new RuntimeException("Invalid credentials");
        }

        // Обновление времени последнего входа
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Получение ролей пользователя
        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
        String primaryRole = roles.isEmpty() ? "unknown" : roles.get(0);

        // Создание токена
        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpirationHours);

        ActiveToken activeToken = new ActiveToken(token, user.getId(), user.getEmail(), primaryRole, expiresAt);
        activeTokenRepository.save(activeToken);

        logger.info("User login successful, email: {}, role: {}, token: {}", email, primaryRole, token);

        return createLoginResponse(token, user.getId(), user.getEmail(),
                "User", "Name", primaryRole, "Login successful", roles);
    }

    private LoginResponse handleDemoUserLogin(String email, String role, String firstName, String lastName) {
        Long demoUserId = role.equals("admin") ? 1L : role.equals("teacher") ? 2L : 3L;

        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpirationHours);

        ActiveToken activeToken = new ActiveToken(token, demoUserId, email, role, expiresAt);
        activeTokenRepository.save(activeToken);

        logger.info("Demo {} login successful, token: {}", role, token);
        return createLoginResponse(token, demoUserId, email, firstName, lastName, role, "Login successful", List.of(role));
    }

    public TokenValidationResponse validateToken(String token) {
        logger.info("Validating token: {}", token);

        // Очистка устаревших токенов
        cleanExpiredTokens();

        ActiveToken activeToken = activeTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (activeToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            activeTokenRepository.delete(activeToken);
            throw new RuntimeException("Token expired");
        }

        return new TokenValidationResponse(true, activeToken.getUserRole(), activeToken.getUserId());
    }

    public Long getUserId(String token) {
        ActiveToken activeToken = activeTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        return activeToken.getUserId();
    }

    public String getUserRole(String token) {
        ActiveToken activeToken = activeTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        return activeToken.getUserRole();
    }

    public boolean isValidToken(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
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

    private LoginResponse createLoginResponse(String token, Long userId, String email,
                                              String firstName, String lastName, String role,
                                              String message, List<String> roles) {
        LoginResponse response = new LoginResponse();
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