package nba.studix.userservice.Service;

import nba.studix.userservice.Entity.*;
import nba.studix.userservice.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final SchoolRepository schoolRepository;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository,
                       TeacherRepository teacherRepository, StudentRepository studentRepository,
                       ParentRepository parentRepository, SchoolRepository schoolRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.schoolRepository = schoolRepository;
    }

    // Создание пользователя с ролью
    public User createUserWithRole(User user, Role role) {
        // Проверка email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
        }

        // Сохраняем пользователя
        User savedUser = userRepository.save(user);

        // Создаем роль
        UserRole userRole = new UserRole(savedUser, role);
        userRoleRepository.save(userRole);

        // Создаем специфичную сущность в зависимости от роли
        switch (role) {
            case TEACHER:
                Teacher teacher = new Teacher(savedUser);
                teacherRepository.save(teacher);
                break;
            case STUDENT:
                Student student = new Student(savedUser);
                studentRepository.save(student);
                break;
            case PARENT:
                Parent parent = new Parent(savedUser.getId());
                parentRepository.save(parent);
                break;
            case ADMIN:
                // Админ не требует дополнительной сущности
                break;
        }

        logger.info("Created user: {} with role: {}", savedUser.getEmail(), role);
        return savedUser;
    }

    // Получение пользователя по ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Получение пользователя по email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Получение ролей пользователя
    public List<Role> getUserRoles(Long userId) {
        return userRoleRepository.findRolesByUserId(userId);
    }

    // Проверка пароля (временная реализация)
    public boolean validatePassword(String rawPassword, String storedHash) {
        // Временная реализация - простое сравнение
        // TODO: Реализовать BCrypt хеширование
        return rawPassword != null && rawPassword.equals(storedHash);
    }


    // Обновление профиля пользователя
    public User updateUserProfile(Long userId, String firstName, String lastName,
                                  String patronymic, String bio, ProfileVisibility visibility) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPatronymic(patronymic);
        user.setBio(bio);
        user.setProfileVisibility(visibility);
        user.setLastModifiedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // Обновление пользователя админом
    public User adminUpdateUser(Long userId, String email, String firstName, String lastName,
                                String patronymic, UserStatus status, ProfileVisibility visibility,
                                String bio, Long schoolId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверка email на уникальность
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPatronymic(patronymic);
        user.setStatus(status);
        user.setProfileVisibility(visibility);
        user.setBio(bio);
        user.setSchoolId(schoolId);
        user.setLastModifiedAt(LocalDateTime.now());

        // Смена пароля если указан
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPasswordHash(newPassword); // TODO: Хешировать
        }

        return userRepository.save(user);
    }

    // Получение всех пользователей школы
    public List<User> getUsersBySchool(Long schoolId) {
        return userRepository.findBySchoolId(schoolId);
    }

    // Деактивация пользователя
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.INACTIVE);
        user.setLastModifiedAt(LocalDateTime.now());
        userRepository.save(user);
    }

}
