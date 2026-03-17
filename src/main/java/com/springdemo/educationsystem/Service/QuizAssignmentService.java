package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.CreateQuizAssignmentDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Enum.QuizAttemptStatus;
import com.springdemo.educationsystem.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizAssignmentService {

    private final QuizAssignmentRepository quizAssignmentRepository;
    private final QuizRepository quizRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final StudentRepository studentRepository;
    private final QuizAssignmentStudentRepository quizAssignmentStudentRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public QuizAssignmentService(
            QuizAssignmentRepository quizAssignmentRepository,
            QuizRepository quizRepository,
            SchoolClassRepository schoolClassRepository,
            StudentRepository studentRepository,
            QuizAssignmentStudentRepository quizAssignmentStudentRepository,
            QuizAttemptRepository quizAttemptRepository
    ) {
        this.quizAssignmentRepository = quizAssignmentRepository;
        this.quizRepository = quizRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.studentRepository = studentRepository;
        this.quizAssignmentStudentRepository = quizAssignmentStudentRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    @Transactional
    public QuizAssignment createAssignment(Long teacherId, CreateQuizAssignmentDTO dto) {

        if (dto.getQuizId() == null) {
            throw new RuntimeException("quizId is required");
        }

        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new RuntimeException("startTime and endTime are required");
        }

        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new RuntimeException("startTime must be before endTime");
        }

        if (dto.getClassId() == null && (dto.getStudentIds() == null || dto.getStudentIds().isEmpty())) {
            throw new RuntimeException("You must assign quiz to a class or to specific students");
        }

        long availableMinutes = Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes();

        if (dto.getTimeLimitMinutes() != null && dto.getTimeLimitMinutes() > availableMinutes) {
            throw new RuntimeException("timeLimitMinutes cannot exceed assignment availability window");
        }

        Quiz quiz = quizRepository.findById(dto.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (quiz.getTeacher() == null || !quiz.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You can assign only your own quiz");
        }

        QuizAssignment assignment = new QuizAssignment();
        assignment.setQuiz(quiz);
        assignment.setTeacher(quiz.getTeacher());
        assignment.setStartTime(dto.getStartTime());
        assignment.setEndTime(dto.getEndTime());
        assignment.setTimeLimitMinutes(dto.getTimeLimitMinutes());
        assignment.setActive(true);

        if (dto.getClassId() != null) {
            SchoolClass schoolClass = schoolClassRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            assignment.setSchoolClass(schoolClass);
        }

        QuizAssignment savedAssignment = quizAssignmentRepository.save(assignment);

        if (dto.getStudentIds() != null && !dto.getStudentIds().isEmpty()) {
            for (Long studentId : dto.getStudentIds()) {
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

                QuizAssignmentStudent link = new QuizAssignmentStudent();
                link.setQuizAssignment(savedAssignment);
                link.setStudent(student);
                quizAssignmentStudentRepository.save(link);
            }
        }

        return savedAssignment;
    }

    public List<QuizAssignment> getTeacherAssignments(Long teacherId) {
        return quizAssignmentRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    public List<QuizAssignment> getAvailableAssignmentsForStudent(Long studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        LocalDateTime now = LocalDateTime.now();

        List<QuizAssignment> result = new ArrayList<>();

        if (student.getSchoolClass() != null) {
            result.addAll(
                    quizAssignmentRepository
                            .findBySchoolClassIdAndActiveTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByStartTimeDesc(
                                    student.getSchoolClass().getId(),
                                    now,
                                    now
                            )
            );
        }

        result.addAll(
                quizAssignmentRepository
                        .findByAssignedStudentsStudentIdAndActiveTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByStartTimeDesc(
                                studentId,
                                now,
                                now
                        )
        );

        Map<Long, QuizAssignment> unique = new LinkedHashMap<>();
        for (QuizAssignment assignment : result) {
            unique.put(assignment.getId(), assignment);
        }

        List<QuizAssignment> assignments = new ArrayList<>(unique.values());

        return assignments.stream()
                .filter(a -> {
                    Optional<QuizAttempt> att =
                            quizAttemptRepository.findByQuizAssignmentIdAndStudentId(a.getId(), studentId);

                    return att.isEmpty() || att.get().getStatus() != QuizAttemptStatus.SUBMITTED;
                })
                .toList();
    }

    public QuizAssignment getAssignmentForStudent(Long assignmentId, Long studentId) {

        QuizAssignment assignment = quizAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Quiz assignment not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        LocalDateTime now = LocalDateTime.now();

        if (!Boolean.TRUE.equals(assignment.getActive())) {
            throw new RuntimeException("Assignment is inactive");
        }

        if (now.isBefore(assignment.getStartTime()) || now.isAfter(assignment.getEndTime())) {
            throw new RuntimeException("Quiz is not available now");
        }

        boolean allowedByClass = assignment.getSchoolClass() != null
                && student.getSchoolClass() != null
                && assignment.getSchoolClass().getId().equals(student.getSchoolClass().getId());

        boolean allowedByExplicitStudent = quizAssignmentStudentRepository
                .existsByQuizAssignmentIdAndStudentId(assignmentId, studentId);

        if (!allowedByClass && !allowedByExplicitStudent) {
            throw new RuntimeException("You do not have access to this quiz");
        }

        return assignment;
    }
}