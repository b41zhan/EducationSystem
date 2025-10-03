package com.springdemo.educationsystem.Service;


import com.springdemo.educationsystem.Entity.School;
import com.springdemo.educationsystem.Repository.SchoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SchoolService {
    @Autowired
    private SchoolRepository schoolRepository;

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    public School getSchoolById(Long id) {
        return schoolRepository.findById(id).orElse(null);
    }

    public School createSchool(School school) {
        return schoolRepository.save(school);
    }

    public School updateSchool(Long id, School schoolDetails) {
        School school = schoolRepository.findById(id).orElse(null);
        if (school != null) {
            school.setName(schoolDetails.getName());
            school.setAddress(schoolDetails.getAddress());
            return schoolRepository.save(school);
        }
        return null;
    }

    public void deleteSchool(Long id) {
        schoolRepository.deleteById(id);
    }
}