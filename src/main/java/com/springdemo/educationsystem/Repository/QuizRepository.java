package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
    List<Quiz> findByTeacherIdAndActiveTrueOrderByCreatedAtDesc(Long teacherId);
}