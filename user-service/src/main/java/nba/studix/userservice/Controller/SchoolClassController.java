package nba.studix.userservice.Controller;

import nba.studix.userservice.Entity.SchoolClass;
import nba.studix.userservice.Service.SchoolClassService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/school-classes")
@CrossOrigin("*")
public class SchoolClassController {
    private final SchoolClassService schoolClassService;

    public SchoolClassController(SchoolClassService schoolClassService) {
        this.schoolClassService = schoolClassService;
    }

    @GetMapping
    public List<SchoolClass> getAllClasses() {
        return schoolClassService.getAllClasses();
    }

    @GetMapping("/school/{schoolId}")
    public List<SchoolClass> getClassesBySchool(@PathVariable Long schoolId) {
        return schoolClassService.getClassesBySchool(schoolId);
    }
}