package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.XpEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface XpEventRepository extends JpaRepository<XpEvent, Long> {

    List<XpEvent> findByStudentIdOrderByCreatedAtAsc(Long studentId);

    @Query("SELECT e FROM XpEvent e WHERE e.student.id = :studentId ORDER BY e.createdAt ASC")
    List<XpEvent> getHistory(Long studentId);
}
