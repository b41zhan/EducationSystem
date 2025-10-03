package com.springdemo.educationsystem.Service;


import com.springdemo.educationsystem.Entity.Teacher;
import com.springdemo.educationsystem.Repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {
    @Autowired
    private TeacherRepository teacherRepository;

    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id).orElse(null);
    }

    public Teacher getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId).orElse(null);
    }
}