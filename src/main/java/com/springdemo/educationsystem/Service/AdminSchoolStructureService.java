package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.*;
import com.springdemo.educationsystem.Entity.Parent;
import com.springdemo.educationsystem.Entity.ParentStudent;
import com.springdemo.educationsystem.Entity.School;
import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Entity.Student;
import com.springdemo.educationsystem.Entity.Teacher;
import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Repository.ParentRepository;
import com.springdemo.educationsystem.Repository.ParentStudentRepository;
import com.springdemo.educationsystem.Repository.SchoolClassRepository;
import com.springdemo.educationsystem.Repository.SchoolRepository;
import com.springdemo.educationsystem.Repository.StudentRepository;
import com.springdemo.educationsystem.Repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminSchoolStructureService {

    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final TeacherRepository teacherRepository;

    public AdminSchoolStructureService(SchoolRepository schoolRepository,
                                       SchoolClassRepository schoolClassRepository,
                                       StudentRepository studentRepository,
                                       ParentRepository parentRepository,
                                       ParentStudentRepository parentStudentRepository,
                                       TeacherRepository teacherRepository) {
        this.schoolRepository = schoolRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.parentStudentRepository = parentStudentRepository;
        this.teacherRepository = teacherRepository;
    }

    public List<AdminSchoolClassDTO> getClassesBySchool(Long schoolId) {
        return schoolClassRepository.findBySchoolIdOrderByActiveDescNameAscAcademicYearAsc(schoolId)
                .stream()
                .map(this::toClassDto)
                .collect(Collectors.toList());
    }

    public List<AdminTeacherOptionDTO> getTeachersBySchool(Long schoolId) {
        return teacherRepository.findByUserSchoolIdWithUser(schoolId)
                .stream()
                .map(this::toTeacherOptionDto)
                .collect(Collectors.toList());
    }

    public AdminSchoolClassDTO createClass(AdminSchoolClassCreateDTO dto) {
        validateCreateClass(dto);

        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        boolean duplicate = schoolClassRepository.existsDuplicate(
                dto.getSchoolId(),
                dto.getName().trim(),
                dto.getAcademicYear().trim(),
                null
        );

        if (duplicate) {
            throw new RuntimeException("Class with this name and academic year already exists in the school");
        }

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setSchool(school);
        schoolClass.setName(dto.getName().trim());
        schoolClass.setAcademicYear(dto.getAcademicYear().trim());
        schoolClass.setActive(true);
        schoolClass.setHomeroomTeacher(resolveAndValidateHomeroomTeacher(dto.getHomeroomTeacherId(), school.getId()));

        return toClassDto(schoolClassRepository.save(schoolClass));
    }

    public AdminSchoolClassDTO updateClass(Long classId, AdminSchoolClassUpdateDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        String newName = dto.getName() != null ? dto.getName().trim() : schoolClass.getName();
        String newAcademicYear = dto.getAcademicYear() != null ? dto.getAcademicYear().trim() : schoolClass.getAcademicYear();

        if (newName == null || newName.isBlank()) {
            throw new RuntimeException("Class name is required");
        }

        if (newAcademicYear == null || newAcademicYear.isBlank()) {
            throw new RuntimeException("Academic year is required");
        }

        boolean duplicate = schoolClassRepository.existsDuplicate(
                schoolClass.getSchool().getId(),
                newName,
                newAcademicYear,
                schoolClass.getId()
        );

        if (duplicate) {
            throw new RuntimeException("Another class with this name and academic year already exists in the school");
        }

        schoolClass.setName(newName);
        schoolClass.setAcademicYear(newAcademicYear);

        if (dto.getActive() != null) {
            if (!dto.getActive() && schoolClass.getStudents() != null && !schoolClass.getStudents().isEmpty()) {
                throw new RuntimeException("Cannot archive class while it still has assigned students");
            }
            schoolClass.setActive(dto.getActive());
        }

        schoolClass.setHomeroomTeacher(
                resolveAndValidateHomeroomTeacher(dto.getHomeroomTeacherId(), schoolClass.getSchool().getId())
        );

        return toClassDto(schoolClassRepository.save(schoolClass));
    }

    public AdminSchoolClassDTO archiveClass(Long classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (schoolClass.getStudents() != null && !schoolClass.getStudents().isEmpty()) {
            throw new RuntimeException("Cannot archive class while it still has assigned students");
        }

        schoolClass.setActive(false);
        return toClassDto(schoolClassRepository.save(schoolClass));
    }

    public AdminClassDetailsDTO getClassDetails(Long classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<AdminStudentInClassDTO> students = studentRepository.findBySchoolClassIdWithUser(classId)
                .stream()
                .map(this::toStudentInClassDto)
                .collect(Collectors.toList());

        AdminClassDetailsDTO dto = new AdminClassDetailsDTO();
        dto.setSchoolClass(toClassDto(schoolClass));
        dto.setStudents(students);
        return dto;
    }

    public List<AdminStudentInClassDTO> getStudentsByClass(Long classId) {
        return studentRepository.findBySchoolClassIdWithUser(classId)
                .stream()
                .map(this::toStudentInClassDto)
                .collect(Collectors.toList());
    }

    public List<AdminStudentInClassDTO> getStudentsBySchool(Long schoolId) {
        return studentRepository.findByUserSchoolIdWithUser(schoolId)
                .stream()
                .map(this::toStudentInClassDto)
                .collect(Collectors.toList());
    }

    public List<AdminStudentInClassDTO> getUnassignedStudentsBySchool(Long schoolId) {
        return studentRepository.findByUserSchoolIdAndSchoolClassIsNullWithUser(schoolId)
                .stream()
                .map(this::toStudentInClassDto)
                .collect(Collectors.toList());
    }

    public AdminStudentInClassDTO assignStudentToClass(AdminAssignStudentToClassDTO dto) {
        if (dto == null || dto.getStudentId() == null || dto.getClassId() == null) {
            throw new RuntimeException("Student and class are required");
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        SchoolClass schoolClass = schoolClassRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (student.getSchoolClass() != null) {
            throw new RuntimeException("Student is already assigned to a class");
        }

        if (student.getUser() == null || student.getUser().getSchool() == null) {
            throw new RuntimeException("Student school is not defined");
        }

        if (!Objects.equals(student.getUser().getSchool().getId(), schoolClass.getSchool().getId())) {
            throw new RuntimeException("Student and class must belong to the same school");
        }

        student.setSchoolClass(schoolClass);
        return toStudentInClassDto(studentRepository.save(student));
    }

    public AdminStudentInClassDTO transferStudent(AdminTransferStudentDTO dto) {
        if (dto == null || dto.getStudentId() == null || dto.getToClassId() == null) {
            throw new RuntimeException("Student and target class are required");
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        SchoolClass targetClass = schoolClassRepository.findById(dto.getToClassId())
                .orElseThrow(() -> new RuntimeException("Target class not found"));

        if (student.getUser() == null || student.getUser().getSchool() == null) {
            throw new RuntimeException("Student school is not defined");
        }

        if (!Objects.equals(student.getUser().getSchool().getId(), targetClass.getSchool().getId())) {
            throw new RuntimeException("Student and target class must belong to the same school");
        }

        if (student.getSchoolClass() != null && Objects.equals(student.getSchoolClass().getId(), targetClass.getId())) {
            throw new RuntimeException("Student is already assigned to this class");
        }

        student.setSchoolClass(targetClass);
        return toStudentInClassDto(studentRepository.save(student));
    }

    public AdminParentChildViewDTO linkParentToStudent(AdminParentChildLinkDTO dto) {
        if (dto == null || dto.getParentId() == null || dto.getStudentId() == null) {
            throw new RuntimeException("Parent and student are required");
        }

        Parent parent = parentRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (parent.getUser() == null || parent.getUser().getSchool() == null) {
            throw new RuntimeException("Parent school is not defined");
        }

        if (student.getUser() == null || student.getUser().getSchool() == null) {
            throw new RuntimeException("Student school is not defined");
        }

        if (!Objects.equals(parent.getUser().getSchool().getId(), student.getUser().getSchool().getId())) {
            throw new RuntimeException("Parent and student must belong to the same school");
        }

        boolean exists = parentStudentRepository.existsByParentIdAndStudentId(parent.getId(), student.getId());
        if (exists) {
            throw new RuntimeException("This parent-child link already exists");
        }

        ParentStudent parentStudent = new ParentStudent();
        parentStudent.setParent(parent);
        parentStudent.setStudent(student);

        return toParentChildViewDto(parentStudentRepository.save(parentStudent));
    }

    public void unlinkParentFromStudent(Long parentId, Long studentId) {
        if (parentId == null || studentId == null) {
            throw new RuntimeException("Parent and student are required");
        }

        ParentStudent parentStudent = parentStudentRepository.findByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new RuntimeException("Parent-child link not found"));

        parentStudentRepository.delete(parentStudent);
    }

    public List<AdminStudentInClassDTO> getChildrenByParent(Long parentId) {
        return parentStudentRepository.findByParentId(parentId)
                .stream()
                .map(ParentStudent::getStudent)
                .map(this::toStudentInClassDto)
                .collect(Collectors.toList());
    }

    public List<AdminParentChildViewDTO> getParentsByStudent(Long studentId) {
        return parentStudentRepository.findByStudentId(studentId)
                .stream()
                .map(this::toParentChildViewDto)
                .collect(Collectors.toList());
    }

    public List<AdminParentChildViewDTO> getParentChildLinksBySchool(Long schoolId) {
        return parentStudentRepository.findAll()
                .stream()
                .filter(ps ->
                        ps.getStudent() != null
                                && ps.getStudent().getUser() != null
                                && ps.getStudent().getUser().getSchool() != null
                                && Objects.equals(ps.getStudent().getUser().getSchool().getId(), schoolId)
                )
                .map(this::toParentChildViewDto)
                .collect(Collectors.toList());
    }

    public List<AdminParentChildViewDTO> getAvailableParentsBySchool(Long schoolId) {
        return parentRepository.findByUserSchoolIdWithUser(schoolId)
                .stream()
                .map(parent -> {
                    AdminParentChildViewDTO dto = new AdminParentChildViewDTO();
                    dto.setParentId(parent.getId());
                    dto.setParentName(formatUserFullName(parent.getUser()));
                    dto.setParentEmail(parent.getUser() != null ? parent.getUser().getEmail() : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void validateCreateClass(AdminSchoolClassCreateDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        if (dto.getSchoolId() == null) {
            throw new RuntimeException("School is required");
        }

        if (dto.getName() == null || dto.getName().trim().isBlank()) {
            throw new RuntimeException("Class name is required");
        }

        if (dto.getAcademicYear() == null || dto.getAcademicYear().trim().isBlank()) {
            throw new RuntimeException("Academic year is required");
        }
    }

    private Teacher resolveAndValidateHomeroomTeacher(Long teacherId, Long schoolId) {
        if (teacherId == null) {
            return null;
        }

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Homeroom teacher not found"));

        if (teacher.getUser() == null || teacher.getUser().getSchool() == null) {
            throw new RuntimeException("Selected teacher is not attached to a school");
        }

        if (!Objects.equals(teacher.getUser().getSchool().getId(), schoolId)) {
            throw new RuntimeException("Selected homeroom teacher belongs to another school");
        }

        return teacher;
    }

    private AdminSchoolClassDTO toClassDto(SchoolClass schoolClass) {
        AdminSchoolClassDTO dto = new AdminSchoolClassDTO();
        dto.setId(schoolClass.getId());
        dto.setName(schoolClass.getName());
        dto.setAcademicYear(schoolClass.getAcademicYear());
        dto.setActive(schoolClass.isActive());

        if (schoolClass.getSchool() != null) {
            dto.setSchoolId(schoolClass.getSchool().getId());
            dto.setSchoolName(schoolClass.getSchool().getName());
        }

        dto.setStudentsCount(schoolClass.getStudents() != null ? schoolClass.getStudents().size() : 0);

        if (schoolClass.getHomeroomTeacher() != null) {
            Teacher teacher = schoolClass.getHomeroomTeacher();
            dto.setHomeroomTeacherId(teacher.getId());

            if (teacher.getUser() != null) {
                dto.setHomeroomTeacherUserId(teacher.getUser().getId());
                dto.setHomeroomTeacherFullName(formatUserFullName(teacher.getUser()));
                dto.setHomeroomTeacherEmail(teacher.getUser().getEmail());
            }
        }

        return dto;
    }

    private AdminTeacherOptionDTO toTeacherOptionDto(Teacher teacher) {
        AdminTeacherOptionDTO dto = new AdminTeacherOptionDTO();
        dto.setTeacherId(teacher.getId());

        if (teacher.getUser() != null) {
            dto.setUserId(teacher.getUser().getId());
            dto.setFullName(formatUserFullName(teacher.getUser()));
            dto.setEmail(teacher.getUser().getEmail());
        }

        return dto;
    }

    private AdminStudentInClassDTO toStudentInClassDto(Student student) {
        AdminStudentInClassDTO dto = new AdminStudentInClassDTO();
        dto.setStudentId(student.getId());

        if (student.getUser() != null) {
            dto.setUserId(student.getUser().getId());
            dto.setFullName(formatUserFullName(student.getUser()));
            dto.setEmail(student.getUser().getEmail());

            if (student.getUser().getSchool() != null) {
                dto.setSchoolId(student.getUser().getSchool().getId());
                dto.setSchoolName(student.getUser().getSchool().getName());
            }
        }

        if (student.getSchoolClass() != null) {
            dto.setClassId(student.getSchoolClass().getId());
            dto.setClassName(student.getSchoolClass().getName());
        }

        return dto;
    }

    private AdminParentChildViewDTO toParentChildViewDto(ParentStudent parentStudent) {
        AdminParentChildViewDTO dto = new AdminParentChildViewDTO();

        if (parentStudent.getParent() != null) {
            dto.setParentId(parentStudent.getParent().getId());

            if (parentStudent.getParent().getUser() != null) {
                dto.setParentName(formatUserFullName(parentStudent.getParent().getUser()));
                dto.setParentEmail(parentStudent.getParent().getUser().getEmail());
            }
        }

        if (parentStudent.getStudent() != null) {
            dto.setStudentId(parentStudent.getStudent().getId());

            if (parentStudent.getStudent().getUser() != null) {
                dto.setStudentName(formatUserFullName(parentStudent.getStudent().getUser()));
                dto.setStudentEmail(parentStudent.getStudent().getUser().getEmail());
            }

            if (parentStudent.getStudent().getSchoolClass() != null) {
                dto.setClassId(parentStudent.getStudent().getSchoolClass().getId());
                dto.setClassName(parentStudent.getStudent().getSchoolClass().getName());
            }
        }

        return dto;
    }

    private String formatUserFullName(User user) {
        if (user == null) {
            return "—";
        }

        return String.join(" ",
                safe(user.getLastName()),
                safe(user.getFirstName()),
                safe(user.getPatronymic())
        ).trim().replaceAll("\\s+", " ");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}