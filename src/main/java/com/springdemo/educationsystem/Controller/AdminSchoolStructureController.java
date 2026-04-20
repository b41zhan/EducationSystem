package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.*;
import com.springdemo.educationsystem.Entity.Role;
import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Repository.UserRepository;
import com.springdemo.educationsystem.Service.AdminSchoolStructureService;
import com.springdemo.educationsystem.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/structure")
@CrossOrigin("*")
public class AdminSchoolStructureController {

    private final AdminSchoolStructureService adminSchoolStructureService;
    private final AuthService authService;
    private final UserRepository userRepository;

    public AdminSchoolStructureController(AdminSchoolStructureService adminSchoolStructureService,
                                          AuthService authService,
                                          UserRepository userRepository) {
        this.adminSchoolStructureService = adminSchoolStructureService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping("/schools/{schoolId}/classes")
    public ResponseEntity<?> getClassesBySchool(@PathVariable Long schoolId,
                                                @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            List<AdminSchoolClassDTO> classes = adminSchoolStructureService.getClassesBySchool(schoolId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PostMapping("/classes")
    public ResponseEntity<?> createClass(@RequestBody AdminSchoolClassCreateDTO dto,
                                         @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.createClass(dto));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/classes/{classId}")
    public ResponseEntity<?> updateClass(@PathVariable Long classId,
                                         @RequestBody AdminSchoolClassUpdateDTO dto,
                                         @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.updateClass(classId, dto));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/classes/{classId}/archive")
    public ResponseEntity<?> archiveClass(@PathVariable Long classId,
                                          @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.archiveClass(classId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/classes/{classId}")
    public ResponseEntity<?> getClassDetails(@PathVariable Long classId,
                                             @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.getClassDetails(classId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/classes/{classId}/students")
    public ResponseEntity<?> getStudentsByClass(@PathVariable Long classId,
                                                @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.getStudentsByClass(classId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/schools/{schoolId}/students")
    public ResponseEntity<?> getStudentsBySchool(@PathVariable Long schoolId,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.getStudentsBySchool(schoolId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/schools/{schoolId}/students/unassigned")
    public ResponseEntity<?> getUnassignedStudentsBySchool(@PathVariable Long schoolId,
                                                           @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.getUnassignedStudentsBySchool(schoolId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/students/assign-class")
    public ResponseEntity<?> assignStudentToClass(@RequestBody AdminAssignStudentToClassDTO dto,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.assignStudentToClass(dto));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/students/transfer-class")
    public ResponseEntity<?> transferStudent(@RequestBody AdminTransferStudentDTO dto,
                                             @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.transferStudent(dto));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PostMapping("/parent-child/link")
    public ResponseEntity<?> linkParentToStudent(@RequestBody AdminParentChildLinkDTO dto,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.linkParentToStudent(dto));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @DeleteMapping("/parent-child/link")
    public ResponseEntity<?> unlinkParentFromStudent(@RequestParam Long parentId,
                                                     @RequestParam Long studentId,
                                                     @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            adminSchoolStructureService.unlinkParentFromStudent(parentId, studentId);
            return ResponseEntity.ok(Map.of("message", "Parent-child link removed successfully"));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/parents/{parentId}/children")
    public ResponseEntity<?> getChildrenByParent(@PathVariable Long parentId,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.getChildrenByParent(parentId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/students/{studentId}/parents")
    public ResponseEntity<?> getParentsByStudent(@PathVariable Long studentId,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.getParentsByStudent(studentId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/schools/{schoolId}/parent-child-links")
    public ResponseEntity<?> getParentChildLinksBySchool(@PathVariable Long schoolId,
                                                         @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.getParentChildLinksBySchool(schoolId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/schools/{schoolId}/parents")
    public ResponseEntity<?> getAvailableParentsBySchool(@PathVariable Long schoolId,
                                                         @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) {
            return forbidden();
        }

        try {
            return ResponseEntity.ok(adminSchoolStructureService.getAvailableParentsBySchool(schoolId));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    private boolean isAdmin(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (token.isBlank() || !authService.isValidToken(token)) {
            return false;
        }

        Long userId = authService.getUserId(token);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || user.getRoles() == null) {
            return false;
        }

        return user.getRoles()
                .stream()
                .map(Role::getName)
                .anyMatch(role -> "admin".equalsIgnoreCase(role));
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return "";
    }

    private ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin rights required."));
    }

    private ResponseEntity<Map<String, String>> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}