package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.QuizAttempt;
import com.springdemo.educationsystem.Enum.QuizAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByQuizAssignmentIdAndStudentId(Long quizAssignmentId, Long studentId);

    List<QuizAttempt> findByQuizAssignmentIdOrderByStartTimeDesc(Long quizAssignmentId);

    List<QuizAttempt> findByStudentIdOrderByStartTimeDesc(Long studentId);

    long countByQuizAssignmentIdAndStatus(Long quizAssignmentId, QuizAttemptStatus status);
    List<QuizAttempt> findByQuizAssignmentId(Long assignmentId);
}