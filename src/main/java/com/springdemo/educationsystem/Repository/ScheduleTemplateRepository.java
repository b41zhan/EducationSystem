package com.springdemo.educationsystem.Repository;

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
public interface ScheduleTemplateRepository extends JpaRepository<ScheduleTemplate, Long> {

    // Найти шаблон по классу и четверти
    List<ScheduleTemplate> findBySchoolClassAndQuarter(SchoolClass schoolClass, Integer quarter);

    // Найти шаблон по классу, четверти и номеру недели
    Optional<ScheduleTemplate> findBySchoolClassAndQuarterAndWeekNumber(SchoolClass schoolClass, Integer quarter, Integer weekNumber);

    // Найти шаблоны по дате (дата попадает в интервал недели)
    @Query("SELECT st FROM ScheduleTemplate st WHERE st.weekStart <= :date AND st.weekEnd >= :date")
    List<ScheduleTemplate> findByDate(@Param("date") LocalDate date);

    // Найти шаблон для конкретного класса на определенную дату
    @Query("SELECT st FROM ScheduleTemplate st WHERE st.schoolClass = :schoolClass AND st.weekStart <= :date AND st.weekEnd >= :date")
    Optional<ScheduleTemplate> findBySchoolClassAndDate(@Param("schoolClass") SchoolClass schoolClass, @Param("date") LocalDate date);
}
