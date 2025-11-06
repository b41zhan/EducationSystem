package nba.studix.authservice.Controller;

import nba.studix.authservice.Client.AdminServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminServiceClient adminServiceClient;

    public AdminController(AdminServiceClient adminServiceClient) {
        this.adminServiceClient = adminServiceClient;
    }

    // Школы
    @GetMapping("/schools")
    public ResponseEntity<?> getSchools(@RequestHeader("Authorization") String token) {
        try {
            List<Map<String, Object>> schools = adminServiceClient.getSchools(token);
            return ResponseEntity.ok(schools);
        } catch (Exception e) {
            logger.error("Error getting schools:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/schools")
    public ResponseEntity<?> createSchool(@RequestHeader("Authorization") String token,
                                          @RequestBody Map<String, Object> school) {
        try {
            Map<String, Object> result = adminServiceClient.createSchool(token, school);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error creating school:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/schools/count")
    public ResponseEntity<?> getSchoolsCount(@RequestHeader("Authorization") String token) {
        try {
            Long count = adminServiceClient.getSchoolsCount(token);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error getting schools count:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Классы
    @GetMapping("/classes")
    public ResponseEntity<?> getClasses(@RequestHeader("Authorization") String token) {
        try {
            List<Map<String, Object>> classes = adminServiceClient.getClasses(token);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            logger.error("Error getting classes:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/classes")
    public ResponseEntity<?> createClass(@RequestHeader("Authorization") String token,
                                         @RequestBody Map<String, Object> schoolClass) {
        try {
            Map<String, Object> result = adminServiceClient.createClass(token, schoolClass);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error creating class:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/classes/count")
    public ResponseEntity<?> getClassesCount(@RequestHeader("Authorization") String token) {
        try {
            Long count = adminServiceClient.getClassesCount(token);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error getting classes count:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Предметы
    @GetMapping("/subjects")
    public ResponseEntity<?> getSubjects(@RequestHeader("Authorization") String token) {
        try {
            List<Map<String, Object>> subjects = adminServiceClient.getSubjects(token);
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            logger.error("Error getting subjects:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/subjects")
    public ResponseEntity<?> createSubject(@RequestHeader("Authorization") String token,
                                           @RequestBody Map<String, Object> subject) {
        try {
            Map<String, Object> result = adminServiceClient.createSubject(token, subject);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error creating subject:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/subjects/count")
    public ResponseEntity<?> getSubjectsCount(@RequestHeader("Authorization") String token) {
        try {
            Long count = adminServiceClient.getSubjectsCount(token);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error getting subjects count:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Пользователи
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader("Authorization") String token) {
        try {
            List<Map<String, Object>> users = adminServiceClient.getUsers(token);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error getting users:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/stats/counts")
    public ResponseEntity<?> getUserCounts(@RequestHeader("Authorization") String token) {
        try {
            Map<String, Long> counts = adminServiceClient.getUserCounts(token);
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            logger.error("Error getting user counts:", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}