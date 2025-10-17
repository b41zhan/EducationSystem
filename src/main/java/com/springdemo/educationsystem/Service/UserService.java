package com.springdemo.educationsystem.Service;
import com.springdemo.educationsystem.DTO.UserDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final SchoolClassRepository classRepository;
    private final SchoolRepository schoolRepository;
    public UserService (UserRepository userRepository, RoleRepository roleRepository,
                        StudentRepository studentRepository, TeacherRepository teacherRepository,
                        ParentRepository parentRepository, SchoolRepository schoolRepository, SchoolClassRepository classRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.parentRepository = parentRepository;
        this.schoolRepository = schoolRepository;
        this.classRepository = classRepository;
    }

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPatronymic(user.getPatronymic());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getSchool() != null) {
            dto.setSchoolId(user.getSchool().getId());
            dto.setSchoolName(user.getSchool().getName());
        }

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> roleNames = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList());
            dto.setRoles(roleNames);
        }

        return dto;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return user != null ? convertToDTO(user) : null;
    }

    public UserDTO registerStudent(User user, Long classId) {
        Role studentRole = roleRepository.findByName("student")
                .orElseThrow(() -> new RuntimeException("Role 'student' not found"));

        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (user.getSchool() == null) {
            user.setSchool(schoolClass.getSchool());
        }

        user.getRoles().add(studentRole);
        User savedUser = userRepository.save(user);

        Student student = new Student();
        student.setUser(savedUser);
        student.setSchoolClass(schoolClass);
        studentRepository.save(student);

        return convertToDTO(savedUser);
    }

    public UserDTO registerTeacher(User user) {
        Role teacherRole = roleRepository.findByName("teacher")
                .orElseThrow(() -> new RuntimeException("Role 'teacher' not found"));

        if (user.getSchool() == null) {
            School defaultSchool = schoolRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Default school not found"));
            user.setSchool(defaultSchool);
        }

        user.getRoles().add(teacherRole);
        User savedUser = userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setUser(savedUser);
        teacherRepository.save(teacher);

        return convertToDTO(savedUser);
    }

    public UserDTO registerParent(User user) {
        Role parentRole = roleRepository.findByName("parent")
                .orElseThrow(() -> new RuntimeException("Role 'parent' not found"));

        user.getRoles().add(parentRole);
        User savedUser = userRepository.save(user);

        Parent parent = new Parent();
        parent.setUser(savedUser);
        parentRepository.save(parent);

        return convertToDTO(savedUser);
    }

    public void updateUserBio(Long userId, String bio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBio(bio);
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
            userRepository.save(user);

            return relativePath;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile photo", e);
        }
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteProfilePhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProfilePhotoPath() != null) {
            try {
                Path filePath = Paths.get(uploadDir, user.getProfilePhotoPath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Failed to delete file: " + e.getMessage());
            }

            user.setProfilePhotoPath(null);
            userRepository.save(user);
        }
    }
}
