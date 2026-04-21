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
    private final TeacherClassSubjectService teacherClassSubjectService;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             SubjectRepository subjectRepository,
                             SchoolClassRepository schoolClassRepository,
                             TeacherRepository teacherRepository,
                             TeacherClassSubjectService teacherClassSubjectService) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.teacherRepository = teacherRepository;
        this.teacherClassSubjectService = teacherClassSubjectService;
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
            dto.setTeacherName(
                    assignment.getTeacher().getUser().getFirstName() + " " +
                            assignment.getTeacher().getUser().getLastName()
            );
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

        // Stage 5: теперь учитель может создавать задания только по назначенной ему паре class+subject
        teacherClassSubjectService.requireTeacherHasClassSubjectByTeacherEntityId(
                teacher.getId(),
                schoolClass.getId(),
                subject.getId()
        );

        Assignment assignment = new Assignment();
        assignment.setTitle(createDTO.getTitle());
        assignment.setDescription(createDTO.getDescription());
        assignment.setMaxGrade(createDTO.getMaxGrade());
        assignment.setDeadline(createDTO.getDeadline());
        assignment.setType(createDTO.getType());
        assignment.setSubject(subject);
        assignment.setTeacher(teacher);
        assignment.setSchoolClass(schoolClass);

        return assignmentRepository.save(assignment);
    }

    public List<AssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll()
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

    public List<AssignmentDTO> getAssignmentsByClass(Long classId) {
        return assignmentRepository.findBySchoolClassId(classId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AssignmentDTO getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public Assignment updateAssignment(Long id, Assignment assignmentDetails) {
        return assignmentRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(assignmentDetails.getTitle());
                    existing.setDescription(assignmentDetails.getDescription());
                    existing.setMaxGrade(assignmentDetails.getMaxGrade());
                    existing.setDeadline(assignmentDetails.getDeadline());
                    existing.setType(assignmentDetails.getType());

                    Long effectiveClassId = existing.getSchoolClass() != null ? existing.getSchoolClass().getId() : null;
                    Long effectiveSubjectId = existing.getSubject() != null ? existing.getSubject().getId() : null;

                    if (assignmentDetails.getSchoolClass() != null && assignmentDetails.getSchoolClass().getId() != null) {
                        SchoolClass newClass = schoolClassRepository.findById(assignmentDetails.getSchoolClass().getId())
                                .orElseThrow(() -> new RuntimeException("Class not found"));
                        existing.setSchoolClass(newClass);
                        effectiveClassId = newClass.getId();
                    }

                    if (assignmentDetails.getSubject() != null && assignmentDetails.getSubject().getId() != null) {
                        Subject newSubject = subjectRepository.findById(assignmentDetails.getSubject().getId())
                                .orElseThrow(() -> new RuntimeException("Subject not found"));
                        existing.setSubject(newSubject);
                        effectiveSubjectId = newSubject.getId();
                    }

                    if (existing.getTeacher() != null && effectiveClassId != null && effectiveSubjectId != null) {
                        teacherClassSubjectService.requireTeacherHasClassSubjectByTeacherEntityId(
                                existing.getTeacher().getId(),
                                effectiveClassId,
                                effectiveSubjectId
                        );
                    }

                    return assignmentRepository.save(existing);
                })
                .orElse(null);
    }

    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }
}