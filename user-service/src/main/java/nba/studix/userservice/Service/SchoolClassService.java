package nba.studix.userservice.Service;

import nba.studix.userservice.Entity.SchoolClass;
import nba.studix.userservice.Repository.SchoolClassRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolClassService {
    private final SchoolClassRepository schoolClassRepository;

    public SchoolClassService(SchoolClassRepository schoolClassRepository) {
        this.schoolClassRepository = schoolClassRepository;
    }

    public List<SchoolClass> getAllClasses() {
        return schoolClassRepository.findAll();
    }

    public List<SchoolClass> getClassesBySchool(Long schoolId) {
        return schoolClassRepository.findBySchoolId(schoolId);
    }

    public SchoolClass getClassById(Long id) {
        return schoolClassRepository.findById(id).orElse(null);
    }
}