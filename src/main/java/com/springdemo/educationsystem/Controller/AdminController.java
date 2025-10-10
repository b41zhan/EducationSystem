package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.UserDTO;
import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final AuthService authService;
    public AdminController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }
    private boolean isAdminAuthorized(String authorizationHeader) {
        logger.info("Checking authorization header: {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Invalid authorization header");
            return false;
        }

        String token = authorizationHeader.substring(7);
        logger.info("Token: {}", token);

        String role = authService.getUserRole(token);
        boolean isAdmin = "admin".equals(role);

        logger.info("Is admin: {}", isAdmin);
        return isAdmin;
    }

    @PostMapping("/register/teacher")
    public ResponseEntity<?> registerTeacher(
            @RequestBody User user,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        logger.info("Received register teacher request for email: {}", user.getEmail());
        logger.info("Authorization header: {}", authorizationHeader);

        if (!isAdminAuthorized(authorizationHeader)) {
            logger.error("Access denied - not admin");
            return ResponseEntity.status(403).body("Access denied. Admin rights required.");
        }

        try {
            logger.info("Attempting to register teacher: {}", user.getEmail());
            UserDTO result = userService.registerTeacher(user);
            logger.info("Teacher registered successfully: {}", result.getEmail());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error registering teacher: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(
            @RequestBody User user,
            @RequestParam Long classId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        logger.info("Received register student request for email: {}, classId: {}", user.getEmail(), classId);

        if (!isAdminAuthorized(authorizationHeader)) {
            return ResponseEntity.status(403).body("Access denied. Admin rights required.");
        }

        try {
            UserDTO result = userService.registerStudent(user, classId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error registering student: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    @PostMapping("/register/parent")
    public ResponseEntity<?> registerParent(
            @RequestBody User user,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        logger.info("Received register parent request for email: {}", user.getEmail());

        if (!isAdminAuthorized(authorizationHeader)) {
            return ResponseEntity.status(403).body("Access denied. Admin rights required.");
        }

        try {
            UserDTO result = userService.registerParent(user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error registering parent: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}