package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.QuizAssignmentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAssignmentStudentRepository extends JpaRepository<QuizAssignmentStudent, Long> {
    List<QuizAssignmentStudent> findByStudentId(Long studentId);
    List<QuizAssignmentStudent> findByQuizAssignmentId(Long quizAssignmentId);
    boolean existsByQuizAssignmentIdAndStudentId(Long quizAssignmentId, Long studentId);
}