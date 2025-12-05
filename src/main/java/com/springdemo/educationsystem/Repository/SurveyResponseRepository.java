package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    List<SurveyResponse> findByUserId(Long userId);
    boolean existsBySurveyIdAndUserId(Long surveyId, Long userId);
}
