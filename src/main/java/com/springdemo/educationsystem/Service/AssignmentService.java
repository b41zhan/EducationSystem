package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.Entity.Assignment;
import com.springdemo.educationsystem.Repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AssignmentService {
    @Autowired
    private AssignmentRepository assignmentRepository;

    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public Assignment getAssignmentById(Long id) {
        return assignmentRepository.findById(id).orElse(null);
    }

    public List<Assignment> getAssignmentsByClass(Long classId) {
        return assignmentRepository.findBySchoolClassId(classId);
    }

    public List<Assignment> getAssignmentsByTeacher(Long teacherId) {
        return assignmentRepository.findByTeacherId(teacherId);
    }

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
