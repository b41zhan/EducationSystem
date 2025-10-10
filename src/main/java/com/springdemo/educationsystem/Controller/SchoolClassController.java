package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Repository.SchoolClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/school-classes")
@CrossOrigin("*")
public class SchoolClassController {

    private final SchoolClassRepository schoolClassRepository;
    public SchoolClassController(SchoolClassRepository schoolClassRepository) {
        this.schoolClassRepository = schoolClassRepository;
    }
    @GetMapping
    public List<SchoolClass> getAllClasses() {
        return schoolClassRepository.findAll();
    }

    @GetMapping("/school/{schoolId}")
    public List<SchoolClass> getClassesBySchool(@PathVariable Long schoolId) {
        return schoolClassRepository.findBySchoolId(schoolId);
    }
}