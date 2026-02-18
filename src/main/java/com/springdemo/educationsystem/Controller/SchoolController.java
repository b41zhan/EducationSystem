package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.Entity.School;
import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Repository.SchoolClassRepository;
import com.springdemo.educationsystem.Repository.SchoolRepository;
import com.springdemo.educationsystem.Service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schools")
@CrossOrigin("*")
public class SchoolController {
    private final SchoolService schoolService;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    public SchoolController(SchoolService schoolService, SchoolRepository schoolRepository, SchoolClassRepository schoolClassRepository) {
        this.schoolService = schoolService;
        this.schoolRepository = schoolRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    @GetMapping
    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    @GetMapping("/{schoolId}/classes")
    public List<SchoolClass> getClassesBySchool(@PathVariable Long schoolId) {
        return schoolClassRepository.findBySchoolId(schoolId);
    }


    @GetMapping("/{id}")
    public ResponseEntity<School> getSchoolById(@PathVariable Long id) {
        School school = schoolService.getSchoolById(id);
        return school != null ? ResponseEntity.ok(school) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public School createSchool(@RequestBody School school) {
        return schoolService.createSchool(school);
    }

    @PutMapping("/{id}")
    public ResponseEntity<School> updateSchool(@PathVariable Long id, @RequestBody School schoolDetails) {
        School updatedSchool = schoolService.updateSchool(id, schoolDetails);
        return updatedSchool != null ? ResponseEntity.ok(updatedSchool) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
        return ResponseEntity.ok().build();
    }
}