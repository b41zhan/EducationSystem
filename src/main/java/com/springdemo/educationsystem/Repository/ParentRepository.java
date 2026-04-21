package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {

    @Query("""
            SELECT p FROM Parent p
            JOIN FETCH p.user u
            WHERE u.school.id = :schoolId
            ORDER BY u.lastName, u.firstName
            """)
    List<Parent> findByUserSchoolIdWithUser(@Param("schoolId") Long schoolId);
}