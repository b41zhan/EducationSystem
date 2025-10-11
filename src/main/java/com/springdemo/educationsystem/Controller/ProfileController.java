package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Service.AuthService;
import com.springdemo.educationsystem.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    // Получить данные профиля
    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            if (!authService.isValidToken(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Long userId = authService.getUserId(token);
            User user = userService.findById(userId);

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            Map<String, Object> profileData = new HashMap<>();
            profileData.put("firstName", user.getFirstName());
            profileData.put("lastName", user.getLastName());
            profileData.put("email", user.getEmail());
            profileData.put("profilePhotoPath", user.getProfilePhotoPath());
            profileData.put("bio", user.getBio());

            return ResponseEntity.ok(profileData);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Обновить "кратко о себе"
    @PutMapping("/bio")
    public ResponseEntity<?> updateBio(@RequestBody Map<String, String> request,
                                       @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            if (!authService.isValidToken(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Long userId = authService.getUserId(token);
            String bio = request.get("bio");

            userService.updateUserBio(userId, bio);

            return ResponseEntity.ok(Map.of("message", "Bio updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Загрузить аватарку
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            if (!authService.isValidToken(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Long userId = authService.getUserId(token);

            // Здесь должна быть логика сохранения файла и получения пути
            String filePath = userService.saveProfilePhoto(userId, file);

            return ResponseEntity.ok(Map.of(
                    "message", "Avatar uploaded successfully",
                    "filePath", filePath
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    // Удалить аватарку
    @DeleteMapping("/avatar")
    public ResponseEntity<?> deleteAvatar(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            if (!authService.isValidToken(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Long userId = authService.getUserId(token);
            userService.deleteProfilePhoto(userId);

            return ResponseEntity.ok(Map.of("message", "Avatar deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }
}
