package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.JournalFinalGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalFinalGradeRepository extends JpaRepository<JournalFinalGrade, Long> {

    List<JournalFinalGrade> findByTeacherIdAndSchoolClassIdAndSubjectIdAndQuarter(
            Long teacherId, Long classId, Long subjectId, Integer quarter
    );

    Optional<JournalFinalGrade> findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndQuarter(
            Long teacherId, Long studentId, Long classId, Long subjectId, Integer quarter
    );

    List<JournalFinalGrade> findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdOrderByQuarterAsc(
            Long teacherId, Long studentId, Long classId, Long subjectId
    );
}