package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.AssignmentDTO;
import com.springdemo.educationsystem.DTO.CreateAssignmentDTO;
import com.springdemo.educationsystem.Entity.Assignment;
import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Entity.Subject;
import com.springdemo.educationsystem.Entity.Teacher;
import com.springdemo.educationsystem.Repository.AssignmentRepository;
import com.springdemo.educationsystem.Repository.SchoolClassRepository;
import com.springdemo.educationsystem.Repository.SubjectRepository;
import com.springdemo.educationsystem.Repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TeacherRepository teacherRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, SubjectRepository subjectRepository,
                             SchoolClassRepository schoolClassRepository, TeacherRepository teacherRepository) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.teacherRepository = teacherRepository;
    }

    public AssignmentDTO convertToDTO(Assignment assignment) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(assignment.getId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setMaxGrade(assignment.getMaxGrade());
        dto.setDeadline(assignment.getDeadline());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setType(assignment.getType());

        if (assignment.getSubject() != null) {
            dto.setSubjectId(assignment.getSubject().getId());
            dto.setSubjectName(assignment.getSubject().getName());
        }

        if (assignment.getTeacher() != null && assignment.getTeacher().getUser() != null) {
            dto.setTeacherId(assignment.getTeacher().getId());
            dto.setTeacherName(assignment.getTeacher().getUser().getFirstName() + " " +
                    assignment.getTeacher().getUser().getLastName());
        }

        if (assignment.getSchoolClass() != null) {
            dto.setClassId(assignment.getSchoolClass().getId());
            dto.setClassName(assignment.getSchoolClass().getName());
        }

        return dto;
    }

    public Assignment createAssignmentWithDTO(CreateAssignmentDTO createDTO, Long teacherId) {
        Subject subject = subjectRepository.findById(createDTO.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + createDTO.getSubjectId()));

        SchoolClass schoolClass = schoolClassRepository.findById(createDTO.getClassId())
                .orElseThrow(() -> new RuntimeException("SchoolClass not found with id: " + createDTO.getClassId()));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        Assignment assignment = new Assignment();
        assignment.setTitle(createDTO.getTitle());
        assignment.setDescription(createDTO.getDescription());
        assignment.setMaxGrade(createDTO.getMaxGrade());
        assignment.setDeadline(createDTO.getDeadline());
        assignment.setType(createDTO.getType());
        assignment.setSubject(subject);
        assignment.setSchoolClass(schoolClass);
        assignment.setTeacher(teacher);

        return assignmentRepository.save(assignment);
    }

    public List<AssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AssignmentDTO getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findById(id).orElse(null);
        return assignment != null ? convertToDTO(assignment) : null;
    }

    public List<AssignmentDTO> getAssignmentsByClass(Long classId) {
        return assignmentRepository.findBySchoolClassId(classId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AssignmentDTO> getAssignmentsByTeacher(Long teacherId) {
        return assignmentRepository.findByTeacherId(teacherId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Старый метод (можно оставить для обратной совместимости)
    public Assignment createAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public Assignment updateAssignment(Long id, Assignment assignmentDetails) {
        Assignment assignment = assignmentRepository.findById(id).orElse(null);
        if (assignment != null) {
            assignment.setTitle(assignmentDetails.getTitle());
            assignment.setDescription(assignmentDetails.getDescription());
            assignment.setMaxGrade(assignmentDetails.getMaxGrade());
            assignment.setDeadline(assignmentDetails.getDeadline());
            assignment.setType(assignmentDetails.getType());
            return assignmentRepository.save(assignment);
        }
        return null;
    }

    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }

}