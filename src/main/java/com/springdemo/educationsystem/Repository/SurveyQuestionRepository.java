package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> { }
