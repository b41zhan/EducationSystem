package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.StudentStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentStatsRepository extends JpaRepository<StudentStats, Long> {

    Optional<StudentStats> findByStudentId(Long studentId);

    @Query("SELECT ss FROM StudentStats ss ORDER BY ss.totalXp DESC")
    List<StudentStats> findAllOrderByTotalXpDesc();

    // ИСПРАВЛЕННЫЙ ЗАПРОС ДЛЯ ПОЗИЦИИ В РЕЙТИНГЕ:
    @Query("SELECT COUNT(ss) + 1 FROM StudentStats ss WHERE ss.totalXp > " +
            "(SELECT ss2.totalXp FROM StudentStats ss2 WHERE ss2.student.id = :studentId)")
    Integer findRankPositionByStudentId(@Param("studentId") Long studentId);

    // ДОБАВЬТЕ ЭТОТ МЕТОД ДЛЯ ПОИСКА ПО user_id:
    @Query("SELECT ss FROM StudentStats ss WHERE ss.student.id = :studentId")
    Optional<StudentStats> findByStudentUserId(@Param("studentId") Long studentId);

}