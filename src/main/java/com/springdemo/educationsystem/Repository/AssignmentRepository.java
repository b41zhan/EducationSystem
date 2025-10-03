package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findBySchoolClassId(Long classId);
    List<Assignment> findByTeacherId(Long teacherId);
    List<Assignment> findByType(String type);
}