// ScheduleDayRepository.java
package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.ScheduleDay;
import com.springdemo.educationsystem.Entity.ScheduleTemplate;
import com.springdemo.educationsystem.Entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleDayRepository extends JpaRepository<ScheduleDay, Long> {

    // Найти дни по шаблону
    List<ScheduleDay> findByTemplate(ScheduleTemplate template);

    // Найти день по дате
    Optional<ScheduleDay> findByDate(LocalDate date);

    // Найти дни по классу и дате
    @Query("SELECT sd FROM ScheduleDay sd WHERE sd.template.schoolClass = :schoolClass AND sd.date = :date")
    Optional<ScheduleDay> findBySchoolClassAndDate(@Param("schoolClass") SchoolClass schoolClass, @Param("date") LocalDate date);

    // Найти дни по классу в диапазоне дат
    @Query("SELECT sd FROM ScheduleDay sd WHERE sd.template.schoolClass = :schoolClass AND sd.date BETWEEN :startDate AND :endDate ORDER BY sd.date")
    List<ScheduleDay> findBySchoolClassAndDateRange(@Param("schoolClass") SchoolClass schoolClass,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}