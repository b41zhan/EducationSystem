package nba.studix.assignmentservice.Controller;

import nba.studix.assignmentservice.DTO.SubjectDTO;
import nba.studix.assignmentservice.Entity.Subject;
import nba.studix.assignmentservice.Service.SubjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin("*")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<SubjectDTO> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> getSubjectById(@PathVariable Long id) {
        SubjectDTO subject = subjectService.getSubjectById(id);
        return subject != null ? ResponseEntity.ok(subject) : ResponseEntity.notFound().build();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<SubjectDTO> getSubjectByName(@PathVariable String name) {
        SubjectDTO subject = subjectService.getSubjectByName(name);
        return subject != null ? ResponseEntity.ok(subject) : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public List<SubjectDTO> searchSubjects(@RequestParam String name) {
        return subjectService.searchSubjectsByName(name);
    }

    @PostMapping
    public SubjectDTO createSubject(@RequestBody Subject subject) {
        return subjectService.createSubject(subject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectDTO> updateSubject(@PathVariable Long id, @RequestBody Subject subjectDetails) {
        SubjectDTO updatedSubject = subjectService.updateSubject(id, subjectDetails);
        return updatedSubject != null ? ResponseEntity.ok(updatedSubject) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.ok().build();
    }
}