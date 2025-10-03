package com.springdemo.educationsystem.Repository;
import com.springdemo.educationsystem.Entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
}