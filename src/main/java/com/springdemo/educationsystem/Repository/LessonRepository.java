// LessonRepository.java
package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Lesson;
import com.springdemo.educationsystem.Entity.ScheduleDay;
import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Найти уроки по дню расписания
    List<Lesson> findByDay(ScheduleDay day);

    // Найти уроки по дню, отсортированные по номеру урока
    List<Lesson> findByDayOrderByLessonNumber(ScheduleDay day);

    // Найти уроки учителя на определенную дату
    @Query("SELECT l FROM Lesson l WHERE l.teacher = :teacher AND l.day.date = :date ORDER BY l.lessonNumber")
    List<Lesson> findByTeacherAndDate(@Param("teacher") Teacher teacher, @Param("date") LocalDate date);

    // Найти уроки учителя в диапазоне дат
    @Query("SELECT l FROM Lesson l WHERE l.teacher = :teacher AND l.day.date BETWEEN :startDate AND :endDate ORDER BY l.day.date, l.lessonNumber")
    List<Lesson> findByTeacherAndDateRange(@Param("teacher") Teacher teacher,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    // Найти уроки класса на определенную дату
    @Query("SELECT l FROM Lesson l WHERE l.day.template.schoolClass = :schoolClass AND l.day.date = :date ORDER BY l.lessonNumber")
    List<Lesson> findBySchoolClassAndDate(@Param("schoolClass") SchoolClass schoolClass, @Param("date") LocalDate date);

    // Проверить наличие конфликта для учителя в определенное время
    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE " +
            "l.teacher.id = :teacherId AND l.day.date = :date AND " +
            "((l.startTime <= :endTime AND l.endTime >= :startTime))")
    boolean existsTeacherTimeConflict(@Param("teacherId") Long teacherId,
                                      @Param("date") LocalDate date,
                                      @Param("startTime") LocalTime startTime,
                                      @Param("endTime") LocalTime endTime);

    // Найти уроки по учителю и предмету
    @Query("SELECT l FROM Lesson l WHERE l.teacher.id = :teacherId AND l.subject.id = :subjectId ORDER BY l.day.date, l.startTime")
    List<Lesson> findByTeacherAndSubject(@Param("teacherId") Long teacherId, @Param("subjectId") Long subjectId);
}