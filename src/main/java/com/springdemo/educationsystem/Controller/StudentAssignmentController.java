package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.DTO.SubmissionDTO;
import com.springdemo.educationsystem.Entity.Assignment;
import com.springdemo.educationsystem.Entity.Submission;
import com.springdemo.educationsystem.Entity.Student;
import com.springdemo.educationsystem.Repository.AssignmentRepository;
import com.springdemo.educationsystem.Repository.SubmissionRepository;
import com.springdemo.educationsystem.Repository.StudentRepository;
import com.springdemo.educationsystem.Service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/assignments")
@CrossOrigin("*")
public class StudentAssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final AuthService authService;

    public StudentAssignmentController(
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            StudentRepository studentRepository,
            AuthService authService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
        this.authService = authService;
    }

    private Long getStudentId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserId(token);
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return student.getId();
    }

    // -------------------------------
    // 1️⃣ Задания, которые нужно сдать (НЕ сдано, дедлайн не прошёл)
    // -------------------------------
    @GetMapping("/to-submit")
    public List<Assignment> getAssignmentsToSubmit(@RequestHeader("Authorization") String auth) {
        Long studentId = getStudentId(auth);

        return assignmentRepository.findAll()
                .stream()
                .filter(a -> submissionRepository.findByAssignmentIdAndStudentId(a.getId(), studentId).isEmpty())
                .filter(a -> a.getDeadline().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    // -------------------------------
    // 2️⃣ Активные задания (сдано, но НЕ оценено)
    // -------------------------------
    @GetMapping("/active")
    public List<SubmissionDTO> getActiveAssignments(@RequestHeader("Authorization") String auth) {
        Long studentId = getStudentId(auth);

        return submissionRepository.findByStudentId(studentId)
                .stream()
                .filter(s -> s.getStatus().equals("submitted"))
                .map(s -> new SubmissionDTO(
                        s.getId(),
                        s.getAssignment().getId(),
                        s.getAssignment().getTitle(),
                        studentId,
                        s.getStudent().getUser().getFirstName() + " " + s.getStudent().getUser().getLastName(),
                        s.getFileName(),
                        s.getFileSize(),
                        s.getSubmittedAt(),
                        s.getStatus(),
                        s.getComment(),
                        null,        // Оценки пока нет
                        null         // Комментария учителя пока нет
                ))
                .collect(Collectors.toList());

    }

    // -------------------------------
    // 3️⃣ Просроченные (не сдано & дедлайн прошёл)
    // -------------------------------
    @GetMapping("/overdue")
    public List<Assignment> getOverdueAssignments(@RequestHeader("Authorization") String auth) {
        Long studentId = getStudentId(auth);

        return assignmentRepository.findAll()
                .stream()
                .filter(a -> a.getDeadline().isBefore(LocalDateTime.now()))
                .filter(a -> submissionRepository.findByAssignmentIdAndStudentId(a.getId(), studentId).isEmpty())
                .collect(Collectors.toList());
    }
}

