package nba.studix.userservice.Service;

import nba.studix.userservice.Entity.School;
import nba.studix.userservice.Entity.SchoolClass;
import nba.studix.userservice.Repository.SchoolClassRepository;
import nba.studix.userservice.Repository.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final SchoolRepository schoolRepository;

    public SchoolClassService(SchoolClassRepository schoolClassRepository, SchoolRepository schoolRepository) {
        this.schoolClassRepository = schoolClassRepository;
        this.schoolRepository = schoolRepository;
    }

    public List<SchoolClass> getAllClasses() {
        return schoolClassRepository.findAll();
    }

    public SchoolClass createClass(SchoolClass schoolClass) {
        // Если передан schoolId, находим школу
        if (schoolClass.getSchool() != null && schoolClass.getSchool().getId() != null) {
            School school = schoolRepository.findById(schoolClass.getSchool().getId())
                    .orElseThrow(() -> new RuntimeException("School not found"));
            schoolClass.setSchool(school);
        }
        return schoolClassRepository.save(schoolClass);
    }

    public long getClassesCount() {
        return schoolClassRepository.count();
    }
}