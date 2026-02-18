package com.springdemo.educationsystem.Service;
import com.springdemo.educationsystem.DTO.RegisterParentRequest;
import com.springdemo.educationsystem.DTO.UpdateUserDTO;
import com.springdemo.educationsystem.DTO.UserDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ParentStudentRepository parentStudentRepository;
    public UserService (UserRepository userRepository, RoleRepository roleRepository,
                        StudentRepository studentRepository, TeacherRepository teacherRepository,
                        ParentRepository parentRepository, SchoolRepository schoolRepository, SchoolClassRepository classRepository,
                        ParentStudentRepository parentStudentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.parentRepository = parentRepository;
        this.schoolRepository = schoolRepository;
        this.classRepository = classRepository;
        this.parentStudentRepository = parentStudentRepository;
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

    public User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public UserDTO registerTeacher(User user, Long schoolId) {
        Role teacherRole = roleRepository.findByName("teacher")
                .orElseThrow(() -> new RuntimeException("Role teacher not found"));

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found: " + schoolId));

        user.setSchool(school);

        if (user.getRoles() == null) user.setRoles(new ArrayList<>());
        user.getRoles().add(teacherRole);

        User savedUser = userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setUser(savedUser);
        teacherRepository.save(teacher);

        return convertToDTO(savedUser);
    }

    public UserDTO registerTeacher(User user) {
        School school = schoolRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No schools found. Create a school first."));

        return registerTeacher(user, school.getId());
    }

    @Transactional
    public UserDTO registerParent(RegisterParentRequest req) {

        Role parentRole = roleRepository.findByName("parent")
                .orElseThrow(() -> new RuntimeException("Role parent not found"));

        // 1) create user
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(req.getPasswordHash()); // если есть encoder - используй его
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPatronymic(req.getPatronymic());

        if (user.getRoles() == null) user.setRoles(new ArrayList<>());
        user.getRoles().add(parentRole);

        User savedUser = userRepository.save(user);

        // 2) create parent
        Parent parent = new Parent();
        parent.setUser(savedUser);
        parentRepository.save(parent);

        // 3) link students in parent_student
        if (req.getStudentIds() == null || req.getStudentIds().isEmpty()) {
            throw new RuntimeException("Select at least one student");
        }

        List<Student> students = studentRepository.findAllById(req.getStudentIds());

        if (students.isEmpty()) {
            throw new RuntimeException("Students not found");
        }

        for (Student s : students) {
            ParentStudent ps = new ParentStudent();
            ps.setParent(parent);
            ps.setStudent(s);
            parentStudentRepository.save(ps);
        }

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


    public UserDTO updateUser(Long userId, UpdateUserDTO updateDTO, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (!user.getEmail().equals(updateDTO.getEmail())) {
            if (userRepository.findByEmail(updateDTO.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists: " + updateDTO.getEmail());
            }
        }

        user.setEmail(updateDTO.getEmail());
        user.setFirstName(updateDTO.getFirstName());
        user.setLastName(updateDTO.getLastName());
        user.setPatronymic(updateDTO.getPatronymic());

        if (updateDTO.getPassword() != null && !updateDTO.getPassword().trim().isEmpty()) {
            user.setPasswordHash(updateDTO.getPassword());
        }

        user.setLastModifiedAt(LocalDateTime.now());
        user.setLastModifiedBy(adminId);

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

}
