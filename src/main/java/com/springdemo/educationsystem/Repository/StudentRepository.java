package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findBySchoolClassId(Long classId);
    // В StudentRepository.java добавить:
    @Query("SELECT s FROM Student s WHERE s.user.id = :userId")
    Optional<Student> findByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Student s " +
            "JOIN FETCH s.user u " +
            "WHERE s.schoolClass.id = :classId " +
            "ORDER BY u.lastName, u.firstName")
    List<Student> findBySchoolClassIdWithUser(@Param("classId") Long classId);
    List<Student> findBySchoolClass_Id(Long classId);



}
