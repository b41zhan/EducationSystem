package nba.studix.submissionservice.Service;

import nba.studix.submissionservice.DTO.SubmissionDTO;
import nba.studix.submissionservice.DTO.GradeDTO;
import nba.studix.submissionservice.DTO.CreateSubmissionDTO;
import nba.studix.submissionservice.Entity.Submission;
import nba.studix.submissionservice.Entity.Grade;
import nba.studix.submissionservice.Repository.SubmissionRepository;
import nba.studix.submissionservice.Repository.GradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubmissionService {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                             GradeRepository gradeRepository) {
        this.submissionRepository = submissionRepository;
        this.gradeRepository = gradeRepository;
    }

    public SubmissionDTO convertToDTO(Submission submission) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(submission.getId());
        dto.setAssignmentId(submission.getAssignmentId());
        dto.setStudentId(submission.getStudentId());
        dto.setFileName(submission.getFileName());
        dto.setFileSize(submission.getFileSize());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setStatus(submission.getStatus());
        dto.setComment(submission.getComment());
        dto.setFilePath(submission.getFilePath());

        // TODO: Получить название задания из assignment-service
        dto.setAssignmentTitle("Assignment Title"); // Заглушка

        // TODO: Получить имя студента из user-service
        dto.setStudentName("Student Name"); // Заглушка

        // Получаем оценку если есть
        gradeRepository.findBySubmissionId(submission.getId()).ifPresent(grade -> {
            dto.setGrade(grade.getGradeValue());
            dto.setTeacherComment(grade.getComment());
        });

        return dto;
    }

    public Submission createSubmission(CreateSubmissionDTO createDTO, Long studentId) {
        // Проверяем, не отправлял ли студент уже работу по этому заданию
        Optional<Submission> existingSubmission = submissionRepository
                .findByAssignmentIdAndStudentId(createDTO.getAssignmentId(), studentId);

        if (existingSubmission.isPresent()) {
            throw new RuntimeException("You have already submitted work for this assignment");
        }

        Submission submission = new Submission();
        submission.setAssignmentId(createDTO.getAssignmentId());
        submission.setStudentId(studentId);
        submission.setFilePath(createDTO.getFilePath());
        submission.setFileName(createDTO.getFileName());
        submission.setFileSize(createDTO.getFileSize());
        submission.setComment(createDTO.getComment());

        Submission savedSubmission = submissionRepository.save(submission);
        logger.info("Created submission for assignment: {} by student: {}",
                createDTO.getAssignmentId(), studentId);

        return savedSubmission;
    }

    public Grade gradeSubmission(GradeDTO gradeDTO, Long teacherId) {
        Submission submission = submissionRepository.findById(gradeDTO.getSubmissionId())
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Проверяем, есть ли уже оценка
        Optional<Grade> existingGrade = gradeRepository.findBySubmissionId(gradeDTO.getSubmissionId());

        Grade grade;
        if (existingGrade.isPresent()) {
            grade = existingGrade.get();
        } else {
            grade = new Grade();
            grade.setSubmissionId(gradeDTO.getSubmissionId());
            grade.setTeacherId(teacherId);
        }

        grade.setGradeValue(gradeDTO.getGradeValue());
        grade.setComment(gradeDTO.getComment());

        // Обновляем статус сдачи
        submission.setStatus("graded");
        submission.setTeacherNotes(gradeDTO.getComment());
        submissionRepository.save(submission);

        Grade savedGrade = gradeRepository.save(grade);
        logger.info("Graded submission: {} by teacher: {} with grade: {}",
                gradeDTO.getSubmissionId(), teacherId, gradeDTO.getGradeValue());

        return savedGrade;
    }

    public List<SubmissionDTO> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SubmissionDTO> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SubmissionDTO> getSubmissionsForTeacher(Long teacherId, List<Long> assignmentIds) {
        // Получаем все сдачи по заданиям учителя
        return submissionRepository.findByAssignmentIds(assignmentIds).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SubmissionDTO getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<SubmissionDTO> getSubmissionsByStatus(String status) {
        return submissionRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getSubmissionStats(Long studentId) {
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);

        long total = submissions.size();
        long submitted = submissions.stream().filter(s -> "submitted".equals(s.getStatus())).count();
        long graded = submissions.stream().filter(s -> "graded".equals(s.getStatus())).count();
        long overdue = submissions.stream().filter(s -> "overdue".equals(s.getStatus())).count();

        return Map.of(
                "total", total,
                "submitted", submitted,
                "graded", graded,
                "overdue", overdue
        );
    }

    public void updateSubmissionStatus(Long submissionId, String status) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        submission.setStatus(status);
        submissionRepository.save(submission);
    }
}
