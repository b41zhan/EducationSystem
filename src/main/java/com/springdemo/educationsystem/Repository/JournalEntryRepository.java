package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.JournalEntry;
import com.springdemo.educationsystem.Entity.JournalEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByTeacherIdAndSchoolClassIdAndSubjectIdAndQuarterOrderByLessonDateAsc(
            Long teacherId, Long classId, Long subjectId, Integer quarter
    );

    Optional<JournalEntry> findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndLessonDateAndEntryTypeAndSourceId(
            Long teacherId,
            Long studentId,
            Long classId,
            Long subjectId,
            LocalDate lessonDate,
            JournalEntryType entryType,
            Long sourceId
    );

    Optional<JournalEntry> findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndLessonDateAndEntryType(
            Long teacherId,
            Long studentId,
            Long classId,
            Long subjectId,
            LocalDate lessonDate,
            JournalEntryType entryType
    );

    List<JournalEntry> findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndQuarter(
            Long teacherId, Long studentId, Long classId, Long subjectId, Integer quarter
    );
}