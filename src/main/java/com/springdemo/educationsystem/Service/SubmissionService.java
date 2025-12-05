package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.SubmissionDTO;
import com.springdemo.educationsystem.DTO.GradeDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    public SubmissionService(SubmissionRepository submissionRepository,
                             GradeRepository gradeRepository,
                             AssignmentRepository assignmentRepository,
                             StudentRepository studentRepository,
                             TeacherRepository teacherRepository,
                             NotificationRepository notificationRepository,
                             UserRepository userRepository,
                             GamificationService gamificationService) {
        this.submissionRepository = submissionRepository;
        this.gradeRepository = gradeRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
    }

    public SubmissionDTO convertToDTO(Submission submission) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(submission.getId());
        dto.setAssignmentId(submission.getAssignment().getId());
        dto.setAssignmentTitle(submission.getAssignment().getTitle());
        dto.setStudentId(submission.getStudent().getId());
        dto.setStudentName(submission.getStudent().getUser().getFirstName() + " " +
                submission.getStudent().getUser().getLastName());
        dto.setFileName(submission.getFileName());
        dto.setFileSize(submission.getFileSize());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setStatus(submission.getStatus());
        dto.setComment(submission.getComment());

        Grade grade = gradeRepository.findBySubmissionId(submission.getId()).orElse(null);
        if (grade != null) {
            dto.setGrade(grade.getGradeValue());
            dto.setTeacherComment(grade.getComment());
        }

        return dto;
    }

    /**
     * Создание/обновление сдачи.
     * Теперь для пары (assignmentId, studentId) существует только ОДНА запись Submission.
     * Если студент пересдаёт работу — обновляем существующую запись.
     */
    public Submission createSubmission(Long assignmentId, Long studentId, String filePath,
                                       String fileName, Long fileSize, String comment) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // ИЩЕМ существующую сдачу этого студента по этому заданию
        Submission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(null);

        if (submission == null) {
            // Первая сдача этого задания данным студентом
            submission = new Submission();
            submission.setAssignment(assignment);
            submission.setStudent(student);
        }

        // В любом случае (первая сдача или пересдача) обновляем данные
        submission.setFilePath(filePath);
        submission.setFileName(fileName);
        submission.setFileSize(fileSize);
        submission.setComment(comment);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus("submitted");

        return submissionRepository.save(submission);
    }

    public Grade gradeSubmission(GradeDTO gradeDTO, Long teacherId) {
        Submission submission = submissionRepository.findById(gradeDTO.getSubmissionId())
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Grade existingGrade = gradeRepository.findBySubmissionId(gradeDTO.getSubmissionId()).orElse(null);

        Grade grade;
        if (existingGrade != null) {
            grade = existingGrade;
        } else {
            grade = new Grade();
            grade.setSubmission(submission);
            grade.setTeacher(teacher);
        }

        grade.setGradeValue(gradeDTO.getGradeValue());
        grade.setComment(gradeDTO.getComment());

        submission.setStatus("graded");
        submissionRepository.save(submission);

        Grade savedGrade = gradeRepository.save(grade);

        createGradeNotification(submission, gradeDTO.getGradeValue());

        // Обновление геймификации
        gamificationService.updateStudentProgress(
                submission.getStudent().getId(),
                submission.getAssignment().getId(),
                gradeDTO.getGradeValue()
        );

        return savedGrade;
    }

    private void createGradeNotification(Submission submission, Integer gradeValue) {
        try {
            User student = submission.getStudent().getUser();
            String message = "Ваша работа \"" + submission.getAssignment().getTitle() + "\" оценена: " + gradeValue + "/100";

            Notification notification = new Notification(student, message, "grade", submission.getId());
            notificationRepository.save(notification);

            logger.info("Created grade notification for student: {}", student.getEmail());

        } catch (Exception e) {
            logger.error("Error creating grade notification: {}", e.getMessage());
        }
    }

    public List<SubmissionDTO> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SubmissionDTO> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SubmissionDTO> getSubmissionsForTeacher(Long teacherId) {
        // Получаем все задания учителя, затем все сдачи этих заданий
        List<Assignment> teacherAssignments = assignmentRepository.findByTeacherId(teacherId);

        return teacherAssignments.stream()
                .flatMap(assignment -> submissionRepository.findByAssignmentId(assignment.getId()).stream())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
