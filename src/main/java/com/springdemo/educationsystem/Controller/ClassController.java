package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.Entity.Student;
import com.springdemo.educationsystem.Repository.StudentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final StudentRepository studentRepository;

    public ClassController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/{classId}/students")
    public List<Student> getStudentsByClass(@PathVariable Long classId) {
        return studentRepository.findBySchoolClassId(classId);
    }
}
