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

    // Преобразование User в UserDTO
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
        // Находим роль студента
        Role studentRole = roleRepository.findByName("student")
                .orElseThrow(() -> new RuntimeException("Role 'student' not found"));

        // Находим класс
        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Сохраняем пользователя
        user.getRoles().add(studentRole);
        User savedUser = userRepository.save(user);

        // Создаем студента
        Student student = new Student();
        student.setUser(savedUser);
        student.setSchoolClass(schoolClass);
        studentRepository.save(student);

        return convertToDTO(savedUser);
    }

    public UserDTO registerTeacher(User user) {
        Role teacherRole = roleRepository.findByName("teacher")
                .orElseThrow(() -> new RuntimeException("Role 'teacher' not found"));

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