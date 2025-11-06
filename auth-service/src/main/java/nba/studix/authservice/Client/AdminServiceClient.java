package nba.studix.authservice.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@FeignClient(name = "api-gateway")
public interface AdminServiceClient {

    // Школы
    @GetMapping("/api/schools")
    List<Map<String, Object>> getSchools(@RequestHeader("Authorization") String token);

    @PostMapping("/api/schools")
    Map<String, Object> createSchool(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> school);

    @GetMapping("/api/schools/count")
    Long getSchoolsCount(@RequestHeader("Authorization") String token);

    // Классы
    @GetMapping("/api/classes")
    List<Map<String, Object>> getClasses(@RequestHeader("Authorization") String token);

    @PostMapping("/api/classes")
    Map<String, Object> createClass(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> schoolClass);

    @GetMapping("/api/classes/count")
    Long getClassesCount(@RequestHeader("Authorization") String token);

    // Предметы
    @GetMapping("/api/subjects")
    List<Map<String, Object>> getSubjects(@RequestHeader("Authorization") String token);

    @PostMapping("/api/subjects")
    Map<String, Object> createSubject(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> subject);

    @GetMapping("/api/subjects/count")
    Long getSubjectsCount(@RequestHeader("Authorization") String token);

    // Пользователи
    @GetMapping("/api/users")
    List<Map<String, Object>> getUsers(@RequestHeader("Authorization") String token);

    @GetMapping("/api/users/stats/counts")
    Map<String, Long> getUserCounts(@RequestHeader("Authorization") String token);
}
