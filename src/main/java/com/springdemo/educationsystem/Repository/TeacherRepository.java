package com.springdemo.educationsystem.Repository;


import com.springdemo.educationsystem.Entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @Query("SELECT t FROM Teacher t WHERE t.user.id = :userId")
    Optional<Teacher> findByUserId(@Param("userId") Long userId);
}
