package nba.studix.userservice.Service;

import nba.studix.userservice.Entity.Student;
import nba.studix.userservice.Entity.SchoolClass;
import nba.studix.userservice.Repository.StudentRepository;
import nba.studix.userservice.Repository.SchoolClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class StudentService {
    private final StudentRepository studentRepository;
    private final SchoolClassRepository schoolClassRepository;

    public StudentService(StudentRepository studentRepository, SchoolClassRepository schoolClassRepository) {
        this.studentRepository = studentRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    // Получение студента по ID пользователя
    public Optional<Student> getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId);
    }

    // Зачисление студента в класс
    public void enrollStudentInClass(Long studentId, Long classId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        student.setSchoolClass(schoolClass);
        studentRepository.save(student);
    }

    // Получение студентов класса
    public List<Student> getStudentsByClass(Long classId) {
        return studentRepository.findByClassId(classId);
    }

    // Генерация studentId
    public String generateStudentId(Student student) {
        String year = String.valueOf(java.time.Year.now().getValue());
        String random = String.format("%04d", new Random().nextInt(10000));
        return year + random;
    }
}
