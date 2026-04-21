package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.CreateQuizAssignmentDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuizAssignmentService {

    private final QuizAssignmentRepository quizAssignmentRepository;
    private final QuizRepository quizRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final StudentRepository studentRepository;
    private final QuizAssignmentStudentRepository quizAssignmentStudentRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final TeacherClassSubjectService teacherClassSubjectService;

    public QuizAssignmentService(QuizAssignmentRepository quizAssignmentRepository,
                                 QuizRepository quizRepository,
                                 SchoolClassRepository schoolClassRepository,
                                 StudentRepository studentRepository,
                                 QuizAssignmentStudentRepository quizAssignmentStudentRepository,
                                 QuizAttemptRepository quizAttemptRepository,
                                 TeacherClassSubjectService teacherClassSubjectService) {
        this.quizAssignmentRepository = quizAssignmentRepository;
        this.quizRepository = quizRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.studentRepository = studentRepository;
        this.quizAssignmentStudentRepository = quizAssignmentStudentRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.teacherClassSubjectService = teacherClassSubjectService;
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

        if (dto.getClassId() != null) {
            teacherClassSubjectService.requireTeacherCanAssignQuizToClassByTeacherEntityId(
                    teacherId,
                    dto.getQuizId(),
                    dto.getClassId()
            );
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

                // Если назначаем конкретному студенту без classId, всё равно валидируем через его класс, если он есть.
                if (assignment.getSchoolClass() == null && student.getSchoolClass() != null) {
                    teacherClassSubjectService.requireTeacherCanAssignQuizToClassByTeacherEntityId(
                            teacherId,
                            dto.getQuizId(),
                            student.getSchoolClass().getId()
                    );
                }

                QuizAssignmentStudent link = new QuizAssignmentStudent();
                link.setQuizAssignment(savedAssignment);
                link.setStudent(student);
                quizAssignmentStudentRepository.save(link);
            }
        }

        return savedAssignment;
    }

    @Transactional(readOnly = true)
    public List<QuizAssignment> getTeacherAssignments(Long teacherId) {
        return quizAssignmentRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    @Transactional(readOnly = true)
    public List<QuizAssignment> getAvailableAssignmentsForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        LocalDateTime now = LocalDateTime.now();

        List<QuizAssignment> all = quizAssignmentRepository.findAllActiveAssignments(now);

        return all.stream()
                .filter(assignment -> {
                    boolean allowedByClass = assignment.getSchoolClass() != null
                            && student.getSchoolClass() != null
                            && assignment.getSchoolClass().getId().equals(student.getSchoolClass().getId());

                    boolean allowedByExplicitStudent =
                            quizAssignmentStudentRepository.existsByQuizAssignmentIdAndStudentId(
                                    assignment.getId(), studentId
                            );

                    return allowedByClass || allowedByExplicitStudent;
                })
                .filter(assignment -> {
                    var existingAttempt = quizAttemptRepository.findByQuizAssignmentIdAndStudentId(
                            assignment.getId(), studentId
                    );
                    return existingAttempt.isEmpty()
                            || !existingAttempt.get().getStatus().name().equals("SUBMITTED");
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizAssignment getAssignmentForStudent(Long assignmentId, Long studentId) {
        QuizAssignment assignment = quizAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

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