package nba.studix.assignmentservice.Service;

import nba.studix.assignmentservice.DTO.SubjectDTO;
import nba.studix.assignmentservice.Entity.Subject;
import nba.studix.assignmentservice.Repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public SubjectDTO convertToDTO(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setDescription(subject.getDescription());
        dto.setCreatedAt(subject.getCreatedAt());
        return dto;
    }

    public List<SubjectDTO> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SubjectDTO getSubjectById(Long id) {
        return subjectRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public SubjectDTO getSubjectByName(String name) {
        return subjectRepository.findByName(name)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public SubjectDTO createSubject(Subject subject) {
        Subject savedSubject = subjectRepository.save(subject);
        return convertToDTO(savedSubject);
    }

    public SubjectDTO updateSubject(Long id, Subject subjectDetails) {
        Subject subject = subjectRepository.findById(id).orElse(null);
        if (subject != null) {
            subject.setName(subjectDetails.getName());
            subject.setDescription(subjectDetails.getDescription());
            Subject savedSubject = subjectRepository.save(subject);
            return convertToDTO(savedSubject);
        }
        return null;
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }

    public List<SubjectDTO> searchSubjectsByName(String name) {
        return subjectRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
