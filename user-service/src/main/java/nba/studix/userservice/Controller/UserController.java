package nba.studix.userservice.Controller;

import nba.studix.userservice.DTO.*;
import nba.studix.userservice.Entity.*;
import nba.studix.userservice.Repository.UserRepository;
import nba.studix.userservice.Repository.UserRoleRepository;
import nba.studix.userservice.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    // Создание пользователя
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserDTO createDTO) {
        try {
            // Создаем User entity
            User user = new User();
            user.setEmail(createDTO.getEmail());
            user.setPasswordHash(createDTO.getPassword()); // TODO: Хешировать пароль
            user.setFirstName(createDTO.getFirstName());
            user.setLastName(createDTO.getLastName());
            user.setPatronymic(createDTO.getPatronymic());
            user.setSchoolId(createDTO.getSchoolId());

            // Конвертируем строку роли в Enum
            Role role = Role.valueOf(createDTO.getRole().toUpperCase());

            // Создаем пользователя с ролью
            User createdUser = userService.createUserWithRole(user, role);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User created successfully");
            response.put("userId", createdUser.getId());
            response.put("email", createdUser.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Получение пользователя по ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Конвертируем в DTO
            UserDTO userDTO = convertToDTO(user);
            return ResponseEntity.ok(userDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Обновление профиля
    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody UpdateUserDTO updateDTO) {
        try {
            User updatedUser = userService.updateUserProfile(
                    id,
                    updateDTO.getFirstName(),
                    updateDTO.getLastName(),
                    updateDTO.getPatronymic(),
                    updateDTO.getBio(),
                    updateDTO.getProfileVisibility()
            );

            UserDTO userDTO = convertToDTO(updatedUser);
            return ResponseEntity.ok(userDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    // Для админа - полное обновление любого пользователя
    @PutMapping("/{id}/admin")
    public ResponseEntity<?> adminUpdateUser(@PathVariable Long id, @RequestBody AdminUpdateUserDTO updateDTO) {
        try {
            // TODO: Проверить права админа
            User updatedUser = userService.adminUpdateUser(
                    id,
                    updateDTO.getEmail(),
                    updateDTO.getFirstName(),
                    updateDTO.getLastName(),
                    updateDTO.getPatronymic(),
                    updateDTO.getStatus(),
                    updateDTO.getProfileVisibility(),
                    updateDTO.getBio(),
                    updateDTO.getSchoolId(),
                    updateDTO.getPassword()
            );

            UserDTO userDTO = convertToDTO(updatedUser);
            return ResponseEntity.ok(userDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Вспомогательный метод для конвертации в DTO
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPatronymic(user.getPatronymic());
        dto.setStatus(user.getStatus());
        dto.setProfileVisibility(user.getProfileVisibility());
        dto.setProfilePhotoPath(user.getProfilePhotoPath());
        dto.setBio(user.getBio());
        dto.setSchoolId(user.getSchoolId());
        dto.setCreatedAt(user.getCreatedAt());

        // Получаем роли пользователя
        var roles = userService.getUserRoles(user.getId());
        dto.setRoles(roles.stream().map(Enum::name).toList());

        return dto;
    }

    @PostMapping("/validate-credentials")
    public ResponseEntity<?> validateCredentials(@RequestBody LoginRequestDTO loginRequest) {
        try {
            logger.info("=== VALIDATE CREDENTIALS CALLED ===");
            logger.info("Email: {}", loginRequest.getEmail());
            logger.info("Password: {}", loginRequest.getPassword());

            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "error", "Email and password are required"
                ));
            }

            // Ищем пользователя по email
            Optional<User> userOpt = userService.getUserByEmail(email);

            if (userOpt.isEmpty()) {
                logger.info("User not found: {}", email);
                return ResponseEntity.status(401).body(Map.of(
                        "valid", false,
                        "error", "User not found"
                ));
            }

            User user = userOpt.get();
            logger.info("User found: {}, status: {}", user.getEmail(), user.getStatus());

            // Простая проверка пароля (временная)
            boolean isValid = password.equals(user.getPasswordHash());
            logger.info("Password valid: {}", isValid);

            if (!isValid) {
                return ResponseEntity.status(401).body(Map.of(
                        "valid", false,
                        "error", "Invalid password"
                ));
            }

            // Проверяем статус пользователя
            if (user.getStatus() != UserStatus.ACTIVE) {
                return ResponseEntity.status(401).body(Map.of(
                        "valid", false,
                        "error", "User is not active"
                ));
            }

            logger.info("Validation successful for user: {}", user.getId());
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "userId", user.getId()
            ));

        } catch (Exception e) {
            logger.error("ERROR in validateCredentials: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "valid", false,
                    "error", "Internal server error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/email/{email}/info")
    public ResponseEntity<?> getUserInfoByEmail(@PathVariable String email) {
        try {
            logger.info("Getting user info for email: {}", email);

            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Конвертируем в DTO
            UserInfoDTO userDTO = new UserInfoDTO();
            userDTO.setId(user.getId());
            userDTO.setEmail(user.getEmail());
            userDTO.setFirstName(user.getFirstName());
            userDTO.setLastName(user.getLastName());

            // Получаем роли пользователя
            List<Role> roles = userService.getUserRoles(user.getId());
            List<String> roleNames = roles.stream().map(Enum::name).collect(Collectors.toList());
            userDTO.setRoles(roleNames);

            return ResponseEntity.ok(userDTO);

        } catch (Exception e) {
            logger.error("Error getting user info for email: {}", email, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    //Arafat poshel nahuy

    @GetMapping("/{id}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable Long id) {
        try {
            List<Role> roles = userService.getUserRoles(id);
            List<String> roleNames = roles.stream().map(Enum::name).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("roles", roleNames));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats/counts")
    public ResponseEntity<?> getUserCounts() {
        try {
            long totalUsers = userRepository.count();
            long teachersCount = userRoleRepository.findByRole(Role.TEACHER).size();
            long studentsCount = userRoleRepository.findByRole(Role.STUDENT).size();
            long parentsCount = userRoleRepository.findByRole(Role.PARENT).size();

            Map<String, Long> counts = Map.of(
                    "totalUsers", totalUsers,
                    "teachers", teachersCount,
                    "students", studentsCount,
                    "parents", parentsCount
            );

            return ResponseEntity.ok(counts);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
