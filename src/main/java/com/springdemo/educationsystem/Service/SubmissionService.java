package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.SubmissionDTO;
import com.springdemo.educationsystem.DTO.GradeDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

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

        // Добавляем оценку если есть
        Grade grade = gradeRepository.findBySubmissionId(submission.getId()).orElse(null);
        if (grade != null) {
            dto.setGrade(grade.getGradeValue());
            dto.setTeacherComment(grade.getComment());
        }

        return dto;
    }

    public Submission createSubmission(Long assignmentId, Long studentId, String filePath,
                                       String fileName, Long fileSize, String comment) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFilePath(filePath);
        submission.setFileName(fileName);
        submission.setFileSize(fileSize);
        submission.setComment(comment);

        return submissionRepository.save(submission);
    }

    public Grade gradeSubmission(GradeDTO gradeDTO, Long teacherId) {
        Submission submission = submissionRepository.findById(gradeDTO.getSubmissionId())
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Проверяем есть ли уже оценка
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

        // Обновляем статус сдачи
        submission.setStatus("graded");
        submissionRepository.save(submission);

        return gradeRepository.save(grade);
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