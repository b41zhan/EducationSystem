package nba.studix.userservice.Service;

import nba.studix.userservice.Entity.*;
import nba.studix.userservice.Repository.TeacherRepository;
import nba.studix.userservice.Repository.SubjectRepository;
import nba.studix.userservice.Repository.SchoolClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;

    public TeacherService(TeacherRepository teacherRepository, SubjectRepository subjectRepository,
                          SchoolClassRepository schoolClassRepository) {
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    // Получение учителя по ID пользователя
    public Optional<Teacher> getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId);
    }

    // Добавление предмета учителю
    public void addSubjectToTeacher(Long teacherId, Long subjectId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        teacher.getSubjects().add(subject);
        teacherRepository.save(teacher);
    }

    // Назначение классным руководителем
    public void assignAsClassTeacher(Long teacherId, Long classId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        teacher.setIsClassTeacher(true);
        teacher.getClassTeacherClasses().add(schoolClass);
        teacherRepository.save(teacher);
    }

    // Получение учителей по предмету
    public List<Teacher> getTeachersBySubject(Long subjectId) {
        return teacherRepository.findBySubjectId(subjectId);
    }

    // Получение предметов учителя
    public Set<Subject> getTeacherSubjects(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return teacher.getSubjects();
    }
}
