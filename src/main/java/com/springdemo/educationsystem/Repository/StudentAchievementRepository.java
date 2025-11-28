package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Student;
import com.springdemo.educationsystem.Entity.StudentAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAchievementRepository extends JpaRepository<StudentAchievement, Long> {
    List<StudentAchievement> findByStudentId(Long studentId);
    Optional<StudentAchievement> findByStudentIdAndAchievementId(Long studentId, Long achievementId);
    boolean existsByStudentIdAndAchievementId(Long studentId, Long achievementId);

    @Query("SELECT COUNT(sa) FROM StudentAchievement sa WHERE sa.student.id = :studentId")
    Long countByStudentId(@Param("studentId") Long studentId);
}