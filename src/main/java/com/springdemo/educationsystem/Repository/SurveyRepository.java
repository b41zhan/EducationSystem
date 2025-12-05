package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findByActiveTrueAndForStudentsTrue();
    List<Survey> findByActiveTrueAndForTeachersTrue();
}


