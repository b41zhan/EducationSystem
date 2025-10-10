package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("SELECT g FROM Grade g WHERE g.submission.id = :submissionId")
    Optional<Grade> findBySubmissionId(@Param("submissionId") Long submissionId);

    @Query("SELECT g FROM Grade g WHERE g.teacher.id = :teacherId")
    List<Grade> findByTeacherId(@Param("teacherId") Long teacherId);
}