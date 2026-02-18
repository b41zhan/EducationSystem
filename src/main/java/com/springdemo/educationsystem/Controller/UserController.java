package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.UserDTO;
import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @PostMapping("/students")
    public UserDTO registerStudent(@RequestBody User user, @RequestParam Long classId) {
        return userService.registerStudent(user, classId);
    }

    @PostMapping("/teachers")
    public UserDTO registerTeacher(@RequestBody User user) {
        return userService.registerTeacher(user);
    }

}
