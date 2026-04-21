package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @Query("SELECT t FROM Teacher t WHERE t.user.id = :userId")
    Optional<Teacher> findByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT t
            FROM Teacher t
            JOIN FETCH t.user u
            WHERE u.school.id = :schoolId
            ORDER BY u.lastName, u.firstName
            """)
    List<Teacher> findByUserSchoolIdWithUser(@Param("schoolId") Long schoolId);
}