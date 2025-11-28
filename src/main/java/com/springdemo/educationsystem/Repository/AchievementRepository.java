package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findByName(String name);
    List<Achievement> findByType(String type);

    @Query("SELECT a FROM Achievement a ORDER BY a.requiredValue ASC")
    List<Achievement> findAllOrderByRequiredValue();
}