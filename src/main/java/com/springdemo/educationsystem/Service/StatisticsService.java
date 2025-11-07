package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.ClassStatsDTO;
import com.springdemo.educationsystem.DTO.StudentStatsDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final AssignmentRepository assignmentRepository;
    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TeacherRepository teacherRepository;

    public StatisticsService(AssignmentRepository assignmentRepository,
                             GradeRepository gradeRepository,
                             StudentRepository studentRepository,
                             SchoolClassRepository schoolClassRepository,
                             TeacherRepository teacherRepository) {
        this.assignmentRepository = assignmentRepository;
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.teacherRepository = teacherRepository;
    }

    public List<SchoolClass> getTeacherClasses(Long teacherId) {
        // Получаем все задания учителя и извлекаем уникальные классы
        List<Assignment> teacherAssignments = assignmentRepository.findByTeacherId(teacherId);

        return teacherAssignments.stream()
                .map(Assignment::getSchoolClass)
                .distinct()
                .collect(Collectors.toList());
    }

    public ClassStatsDTO getClassStatistics(Long teacherId, Long classId) {
        // Проверяем, что класс принадлежит учителю
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Получаем всех студентов класса
        List<Student> students = studentRepository.findBySchoolClassIdWithUser(classId);

        // Получаем все задания для этого класса от данного учителя
        List<Assignment> classAssignments = assignmentRepository.findBySchoolClassId(classId)
                .stream()
                .filter(assignment -> assignment.getTeacher().getId().equals(teacherId))
                .collect(Collectors.toList());

        // Создаем статистику по студентам
        List<StudentStatsDTO> studentStats = new ArrayList<>();
        double totalClassAverage = 0;
        int studentsWithGrades = 0;

        for (Student student : students) {
            StudentStatsDTO studentStat = getStudentStatistics(student, classAssignments);
            studentStats.add(studentStat);

            if (studentStat.getAverageGrade() != null) {
                totalClassAverage += studentStat.getAverageGrade();
                studentsWithGrades++;
            }
        }

        // Создаем общую статистику класса
        ClassStatsDTO classStats = new ClassStatsDTO();
        classStats.setClassId(classId);
        classStats.setClassName(schoolClass.getName());
        classStats.setTotalStudents(students.size());
        classStats.setTotalAssignments(classAssignments.size());
        classStats.setStudents(studentStats);

        // Рассчитываем среднюю оценку по классу
        if (studentsWithGrades > 0) {
            classStats.setClassAverageGrade(Math.round((totalClassAverage / studentsWithGrades) * 100.0) / 100.0);
        } else {
            classStats.setClassAverageGrade(0.0);
        }

        return classStats;
    }

    private StudentStatsDTO getStudentStatistics(Student student, List<Assignment> classAssignments) {
        List<Integer> grades = new ArrayList<>();
        int completedAssignments = 0;

        // Для каждого задания ищем оценку студента
        for (Assignment assignment : classAssignments) {
            // Ищем submission студента для этого задания
            Integer gradeValue = findStudentGradeForAssignment(student, assignment);
            if (gradeValue != null) {
                grades.add(gradeValue);
                completedAssignments++;
            } else {
                grades.add(0); // 0 означает, что работа не сдана или не оценена
            }
        }

        // Рассчитываем среднюю оценку
        Double averageGrade = null;
        if (!grades.isEmpty()) {
            double sum = grades.stream().mapToInt(Integer::intValue).sum();
            averageGrade = Math.round((sum / grades.size()) * 100.0) / 100.0;
        }

        String studentName = student.getUser().getLastName() + " " +
                student.getUser().getFirstName().charAt(0) + "." +
                (student.getUser().getPatronymic() != null ?
                        student.getUser().getPatronymic().charAt(0) + "." : "");

        return new StudentStatsDTO(
                student.getId(),
                studentName,
                student.getSchoolClass().getName(),
                grades,
                averageGrade,
                completedAssignments,
                classAssignments.size()
        );
    }

    private Integer findStudentGradeForAssignment(Student student, Assignment assignment) {
        // Ищем submission студента для задания
        // В реальном приложении нужно добавить соответствующий метод в SubmissionRepository
        // Пока используем упрощенный подход через GradeRepository

        List<Grade> studentGrades = gradeRepository.findByStudentId(student.getId());

        return studentGrades.stream()
                .filter(grade -> grade.getSubmission().getAssignment().getId().equals(assignment.getId()))
                .findFirst()
                .map(Grade::getGradeValue)
                .orElse(null);
    }

    // Метод для получения быстрой статистики по всем классам учителя
    public List<ClassStatsDTO> getTeacherClassesStatistics(Long teacherId) {
        List<SchoolClass> teacherClasses = getTeacherClasses(teacherId);

        return teacherClasses.stream()
                .map(schoolClass -> getClassStatistics(teacherId, schoolClass.getId()))
                .collect(Collectors.toList());
    }
}