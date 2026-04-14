package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.AttendanceMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceMarkRepository extends JpaRepository<AttendanceMark, Long> {

    List<AttendanceMark> findByTeacherIdAndSchoolClassIdAndSubjectIdAndQuarterOrderByLessonDateAsc(
            Long teacherId, Long classId, Long subjectId, Integer quarter
    );

    Optional<AttendanceMark> findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndLessonDate(
            Long teacherId, Long studentId, Long classId, Long subjectId, LocalDate lessonDate
    );
}