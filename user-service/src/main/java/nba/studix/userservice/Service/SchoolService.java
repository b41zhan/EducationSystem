package nba.studix.userservice.Service;

import nba.studix.userservice.Entity.School;
import nba.studix.userservice.Repository.SchoolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolService {
    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

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