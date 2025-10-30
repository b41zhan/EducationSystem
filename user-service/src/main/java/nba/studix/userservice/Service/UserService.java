package nba.studix.userservice.Service;

import nba.studix.userservice.DTO.UserDTO;
import nba.studix.userservice.DTO.CreateUserDTO;
import nba.studix.userservice.DTO.UpdateUserDTO;
import nba.studix.userservice.Entity.*;
import nba.studix.userservice.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public UserService(UserRepository userRepository, SchoolRepository schoolRepository,
                       SchoolClassRepository schoolClassRepository, TeacherRepository teacherRepository,
                       StudentRepository studentRepository, ParentRepository parentRepository,
                       ParentStudentRepository parentStudentRepository) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.parentStudentRepository = parentStudentRepository;
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPatronymic(user.getPatronymic());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setSchoolId(user.getSchoolId());
        dto.setProfilePhotoPath(user.getProfilePhotoPath());
        dto.setBio(user.getBio());

        // Получаем имя школы если есть schoolId
        if (user.getSchoolId() != null) {
            schoolRepository.findById(user.getSchoolId()).ifPresent(school -> {
                dto.setSchoolName(school.getName());
            });
        }

        // Определяем роли пользователя
        List<String> roles = determineUserRoles(user.getId());
        dto.setRoles(roles);

        return dto;
    }

    private List<String> determineUserRoles(Long userId) {
        List<String> roles = new ArrayList<>();

        if (teacherRepository.existsById(userId)) {
            roles.add("teacher");
        }
        if (studentRepository.existsById(userId)) {
            roles.add("student");
        }
        if (parentRepository.existsById(userId)) {
            roles.add("parent");
        }

        return roles;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public UserDTO getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public UserDTO createUser(CreateUserDTO createDTO) {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(createDTO.getEmail())) {
            throw new RuntimeException("User with email " + createDTO.getEmail() + " already exists");
        }

        // Создаем базового пользователя
        User user = new User();
        user.setEmail(createDTO.getEmail());
        user.setFirstName(createDTO.getFirstName());
        user.setLastName(createDTO.getLastName());
        user.setPatronymic(createDTO.getPatronymic());
        user.setSchoolId(createDTO.getSchoolId());

        // Сохраняем пользователя (ID сгенерируется или будет установлен позже)
        User savedUser = userRepository.save(user);

        // Создаем соответствующую сущность в зависимости от роли
        String role = createDTO.getRole();
        switch (role.toLowerCase()) {
            case "teacher":
                Teacher teacher = new Teacher(savedUser.getId());
                teacherRepository.save(teacher);
                break;
            case "student":
                Student student = new Student(savedUser.getId(), createDTO.getClassId());
                studentRepository.save(student);
                break;
            case "parent":
                Parent parent = new Parent(savedUser.getId());
                parentRepository.save(parent);
                break;
            default:
                throw new RuntimeException("Unknown role: " + role);
        }

        logger.info("Created user: {} with role: {}", savedUser.getEmail(), role);
        return convertToDTO(savedUser);
    }

    public UserDTO updateUser(Long userId, UpdateUserDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Проверяем email на уникальность
        if (!user.getEmail().equals(updateDTO.getEmail()) &&
                userRepository.existsByEmail(updateDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + updateDTO.getEmail());
        }

        user.setEmail(updateDTO.getEmail());
        user.setFirstName(updateDTO.getFirstName());
        user.setLastName(updateDTO.getLastName());
        user.setPatronymic(updateDTO.getPatronymic());
        user.setSchoolId(updateDTO.getSchoolId());
        user.setBio(updateDTO.getBio());
        user.setLastModifiedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public void updateUserBio(Long userId, String bio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBio(bio);
        user.setLastModifiedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public String saveProfilePhoto(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            Path uploadPath = Paths.get(uploadDir, "profiles");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" +
                    UUID.randomUUID().toString().substring(0, 8) + "_" +
                    file.getOriginalFilename();

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = "profiles/" + fileName;
            user.setProfilePhotoPath(relativePath);
            user.setLastModifiedAt(LocalDateTime.now());
            userRepository.save(user);

            return relativePath;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile photo", e);
        }
    }

    public void deleteProfilePhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProfilePhotoPath() != null) {
            try {
                Path filePath = Paths.get(uploadDir, user.getProfilePhotoPath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                logger.error("Failed to delete file: {}", e.getMessage());
            }

            user.setProfilePhotoPath(null);
            user.setLastModifiedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<UserDTO> getUsersBySchool(Long schoolId) {
        return userRepository.findBySchoolId(schoolId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}