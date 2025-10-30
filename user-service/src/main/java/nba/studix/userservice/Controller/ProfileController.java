package nba.studix.userservice.Controller;

import nba.studix.userservice.Entity.User;
import nba.studix.userservice.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin("*")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Временная реализация - позже добавим интеграцию с auth-service
            // Сейчас используем заглушку для userId
            Long userId = 1L; // Заглушка

            User user = userService.findEntityById(userId);

            Map<String, Object> profileData = new HashMap<>();
            profileData.put("firstName", user.getFirstName());
            profileData.put("lastName", user.getLastName());
            profileData.put("email", user.getEmail());
            profileData.put("profilePhotoPath", user.getProfilePhotoPath());
            profileData.put("bio", user.getBio());

            if (user.getProfilePhotoPath() != null && !user.getProfilePhotoPath().isEmpty()) {
                String fullImageUrl = "/uploads/" + user.getProfilePhotoPath();
                profileData.put("profilePhotoUrl", fullImageUrl);
            } else {
                profileData.put("profilePhotoUrl", null);
            }

            return ResponseEntity.ok(profileData);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/bio")
    public ResponseEntity<?> updateBio(@RequestBody Map<String, String> request,
                                       @RequestHeader("Authorization") String authorizationHeader) {
        try {
            Long userId = 1L; // Заглушка
            String bio = request.get("bio");

            userService.updateUserBio(userId, bio);
            return ResponseEntity.ok(Map.of("message", "Bio updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          @RequestHeader("Authorization") String authorizationHeader) {
        try {
            Long userId = 1L; // Заглушка

            String filePath = userService.saveProfilePhoto(userId, file);

            return ResponseEntity.ok(Map.of(
                    "message", "Avatar uploaded successfully",
                    "filePath", filePath,
                    "profilePhotoUrl", "/uploads/" + filePath
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<?> deleteAvatar(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            Long userId = 1L; // Заглушка
            userService.deleteProfilePhoto(userId);

            return ResponseEntity.ok(Map.of("message", "Avatar deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}