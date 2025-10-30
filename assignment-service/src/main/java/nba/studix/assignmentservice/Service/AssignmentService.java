package nba.studix.assignmentservice.Service;

import nba.studix.assignmentservice.DTO.AssignmentDTO;
import nba.studix.assignmentservice.DTO.CreateAssignmentDTO;
import nba.studix.assignmentservice.Entity.Assignment;
import nba.studix.assignmentservice.Entity.Subject;
import nba.studix.assignmentservice.Repository.AssignmentRepository;
import nba.studix.assignmentservice.Repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssignmentService {
    private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);

    private final AssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             SubjectRepository subjectRepository) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
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

        dto.setTeacherId(assignment.getTeacherId());
        dto.setClassId(assignment.getClassId());

        // TODO: Добавить получение имени учителя и класса из user-service
        dto.setTeacherName("Teacher"); // Заглушка
        dto.setClassName("Class"); // Заглушка

        return dto;
    }

    public Assignment createAssignmentWithDTO(CreateAssignmentDTO createDTO, Long teacherId) {
        Subject subject = subjectRepository.findById(createDTO.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + createDTO.getSubjectId()));

        Assignment assignment = new Assignment();
        assignment.setTitle(createDTO.getTitle());
        assignment.setDescription(createDTO.getDescription());
        assignment.setMaxGrade(createDTO.getMaxGrade());
        assignment.setDeadline(createDTO.getDeadline());
        assignment.setType(createDTO.getType());
        assignment.setSubject(subject);
        assignment.setTeacherId(teacherId);
        assignment.setClassId(createDTO.getClassId());

        Assignment savedAssignment = assignmentRepository.save(assignment);
        logger.info("Created assignment: {} by teacher: {}", createDTO.getTitle(), teacherId);

        return savedAssignment;
    }

    public List<AssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAllActive().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AssignmentDTO getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<AssignmentDTO> getAssignmentsByClass(Long classId) {
        return assignmentRepository.findByClassId(classId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AssignmentDTO> getAssignmentsByTeacher(Long teacherId) {
        return assignmentRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AssignmentDTO> getAssignmentsBySubject(Long subjectId) {
        return assignmentRepository.findBySubjectId(subjectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AssignmentDTO> getAssignmentsByType(String type) {
        return assignmentRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
        Assignment assignment = assignmentRepository.findById(id).orElse(null);
        if (assignment != null) {
            assignment.setIsActive(false);
            assignmentRepository.save(assignment);
            logger.info("Soft deleted assignment with id: {}", id);
        }
    }

    public List<AssignmentDTO> getOverdueAssignments() {
        return assignmentRepository.findOverdueAssignments().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
