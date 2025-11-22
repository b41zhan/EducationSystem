package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.Entity.Subject;
import com.springdemo.educationsystem.Repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectById(Long id) {
        return subjectRepository.findById(id).orElse(null);
    }

    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public Subject updateSubject(Long id, Subject subjectDetails) {
        Subject subject = subjectRepository.findById(id).orElse(null);
        if (subject != null) {
            subject.setName(subjectDetails.getName());
            return subjectRepository.save(subject);
        }
        return null;
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }
}