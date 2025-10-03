package com.springdemo.educationsystem.Service;
import com.springdemo.educationsystem.DTO.UserDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private ParentRepository parentRepository;
    @Autowired private SchoolClassRepository classRepository;
    @Autowired private SchoolRepository schoolRepository;

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
            School defaultSchool = schoolRepository.findById(2L)
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
}