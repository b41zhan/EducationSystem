package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Entity.Subject;
import com.springdemo.educationsystem.Entity.TeacherClassSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherClassSubjectRepository extends JpaRepository<TeacherClassSubject, Long> {

    @Query("""
            SELECT DISTINCT tcs
            FROM TeacherClassSubject tcs
            JOIN FETCH tcs.teacher t
            JOIN FETCH t.user u
            JOIN FETCH tcs.schoolClass sc
            JOIN FETCH sc.school s
            JOIN FETCH tcs.subject sub
            WHERE s.id = :schoolId
            ORDER BY u.lastName, u.firstName, sc.name, sub.name
            """)
    List<TeacherClassSubject> findBySchoolIdWithRelations(@Param("schoolId") Long schoolId);

    @Query("""
            SELECT DISTINCT tcs
            FROM TeacherClassSubject tcs
            JOIN FETCH tcs.teacher t
            JOIN FETCH t.user u
            JOIN FETCH tcs.schoolClass sc
            JOIN FETCH sc.school s
            JOIN FETCH tcs.subject sub
            WHERE t.id = :teacherId
              AND tcs.active = true
            ORDER BY sc.name, sub.name
            """)
    List<TeacherClassSubject> findActiveByTeacherIdWithRelations(@Param("teacherId") Long teacherId);

    Optional<TeacherClassSubject> findByTeacherIdAndSchoolClassIdAndSubjectIdAndActiveTrue(
            Long teacherId, Long schoolClassId, Long subjectId
    );

    boolean existsByTeacherIdAndSchoolClassIdAndSubjectIdAndActiveTrue(
            Long teacherId, Long schoolClassId, Long subjectId
    );

    boolean existsByTeacherIdAndSchoolClassIdAndActiveTrue(Long teacherId, Long schoolClassId);

    boolean existsByTeacherIdAndSubjectIdAndActiveTrue(Long teacherId, Long subjectId);

    @Query("""
            SELECT DISTINCT sc
            FROM TeacherClassSubject tcs
            JOIN tcs.schoolClass sc
            WHERE tcs.teacher.id = :teacherId
              AND tcs.active = true
            ORDER BY sc.name, sc.academicYear
            """)
    List<SchoolClass> findDistinctActiveClassesByTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
            SELECT DISTINCT sub
            FROM TeacherClassSubject tcs
            JOIN tcs.subject sub
            WHERE tcs.teacher.id = :teacherId
              AND tcs.active = true
            ORDER BY sub.name
            """)
    List<Subject> findDistinctActiveSubjectsByTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
            SELECT DISTINCT tcs
            FROM TeacherClassSubject tcs
            JOIN FETCH tcs.teacher t
            JOIN FETCH t.user u
            JOIN FETCH tcs.schoolClass sc
            JOIN FETCH tcs.subject sub
            WHERE tcs.id = :id
            """)
    Optional<TeacherClassSubject> findByIdWithRelations(@Param("id") Long id);
}