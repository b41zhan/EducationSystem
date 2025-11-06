package nba.studix.userservice.Controller;

import nba.studix.userservice.Entity.School;
import nba.studix.userservice.Service.SchoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schools")
@CrossOrigin("*")
public class SchoolController {

    private static final Logger logger = LoggerFactory.getLogger(SchoolController.class);
    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
    public ResponseEntity<List<School>> getAllSchools() {
        try {
            logger.info("Getting all schools");
            List<School> schools = schoolService.getAllSchools();
            logger.info("Found {} schools", schools.size());
            return ResponseEntity.ok(schools);
        } catch (Exception e) {
            logger.error("Error getting schools", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createSchool(@RequestBody School school) {
        try {
            School createdSchool = schoolService.createSchool(school);
            return ResponseEntity.ok(Map.of(
                    "message", "School created successfully",
                    "schoolId", createdSchool.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getSchoolsCount() {
        try {
            long count = schoolService.getSchoolsCount();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}