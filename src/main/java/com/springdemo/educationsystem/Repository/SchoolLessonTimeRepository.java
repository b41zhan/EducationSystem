// SchoolLessonTimeRepository.java
package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.SchoolLessonTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolLessonTimeRepository extends JpaRepository<SchoolLessonTime, Long> {

    // Найти времена уроков по школе
    List<SchoolLessonTime> findBySchoolIdOrderByLessonNumber(Long schoolId);

    // Найти конкретное время урока по школе и номеру урока
    Optional<SchoolLessonTime> findBySchoolIdAndLessonNumber(Long schoolId, Integer lessonNumber);
}