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

    @Query("SELECT g FROM Grade g " +
            "JOIN g.submission s " +
            "JOIN s.assignment a " +
            "JOIN a.teacher t " +
            "JOIN s.student st " +
            "JOIN st.schoolClass sc " +
            "WHERE t.id = :teacherId AND sc.id = :classId")
    List<Grade> findByTeacherIdAndClassId(@Param("teacherId") Long teacherId,
                                          @Param("classId") Long classId);

    @Query("SELECT g FROM Grade g " +
            "JOIN g.submission s " +
            "JOIN s.student st " +
            "WHERE st.id = :studentId")
    List<Grade> findByStudentId(@Param("studentId") Long studentId);
}