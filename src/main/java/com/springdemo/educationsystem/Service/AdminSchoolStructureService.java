package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.*;
import com.springdemo.educationsystem.Entity.Parent;
import com.springdemo.educationsystem.Entity.ParentStudent;
import com.springdemo.educationsystem.Entity.School;
import com.springdemo.educationsystem.Entity.SchoolClass;
import com.springdemo.educationsystem.Entity.Student;
import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Repository.ParentRepository;
import com.springdemo.educationsystem.Repository.ParentStudentRepository;
import com.springdemo.educationsystem.Repository.SchoolClassRepository;
import com.springdemo.educationsystem.Repository.SchoolRepository;
import com.springdemo.educationsystem.Repository.StudentRepository;
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

    public AdminSchoolStructureService(SchoolRepository schoolRepository,
                                       SchoolClassRepository schoolClassRepository,
                                       StudentRepository studentRepository,
                                       ParentRepository parentRepository,
                                       ParentStudentRepository parentStudentRepository) {
        this.schoolRepository = schoolRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
        this.parentStudentRepository = parentStudentRepository;
    }

    public List<AdminSchoolClassDTO> getClassesBySchool(Long schoolId) {
        return schoolClassRepository.findBySchoolIdOrderByActiveDescNameAscAcademicYearAsc(schoolId)
                .stream()
                .map(this::toClassDto)
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

        if (newName.isBlank()) {
            throw new RuntimeException("Class name is required");
        }

        if (newAcademicYear.isBlank()) {
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

    @Transactional(readOnly = true)
    public AdminClassDetailsDTO getClassDetails(Long classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<AdminStudentInClassDTO> students = studentRepository.findBySchoolClassIdWithUser(classId)
                .stream()
                .map(this::toStudentDto)
                .collect(Collectors.toList());

        AdminClassDetailsDTO dto = new AdminClassDetailsDTO();
        dto.setSchoolClass(toClassDto(schoolClass));
        dto.setStudents(students);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<AdminStudentInClassDTO> getStudentsByClass(Long classId) {
        return studentRepository.findBySchoolClassIdWithUser(classId)
                .stream()
                .map(this::toStudentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminStudentInClassDTO> getStudentsBySchool(Long schoolId) {
        return studentRepository.findByUserSchoolIdWithUser(schoolId)
                .stream()
                .map(this::toStudentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminStudentInClassDTO> getUnassignedStudentsBySchool(Long schoolId) {
        return studentRepository.findByUserSchoolIdAndSchoolClassIsNullWithUser(schoolId)
                .stream()
                .map(this::toStudentDto)
                .collect(Collectors.toList());
    }

    public AdminStudentInClassDTO assignStudentToClass(AdminAssignStudentToClassDTO dto) {
        if (dto == null || dto.getStudentId() == null || dto.getClassId() == null) {
            throw new RuntimeException("studentId and classId are required");
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        SchoolClass targetClass = schoolClassRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (!targetClass.isActive()) {
            throw new RuntimeException("Cannot assign student to archived class");
        }

        User studentUser = student.getUser();
        if (studentUser == null || studentUser.getSchool() == null) {
            throw new RuntimeException("Student school is not defined");
        }

        if (!Objects.equals(studentUser.getSchool().getId(), targetClass.getSchool().getId())) {
            throw new RuntimeException("Student and class belong to different schools");
        }

        student.setSchoolClass(targetClass);
        return toStudentDto(studentRepository.save(student));
    }

    public AdminStudentInClassDTO transferStudent(AdminTransferStudentDTO dto) {
        if (dto == null || dto.getStudentId() == null || dto.getToClassId() == null) {
            throw new RuntimeException("studentId and toClassId are required");
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        SchoolClass targetClass = schoolClassRepository.findById(dto.getToClassId())
                .orElseThrow(() -> new RuntimeException("Target class not found"));

        if (!targetClass.isActive()) {
            throw new RuntimeException("Cannot transfer student to archived class");
        }

        User studentUser = student.getUser();
        if (studentUser == null || studentUser.getSchool() == null) {
            throw new RuntimeException("Student school is not defined");
        }

        if (!Objects.equals(studentUser.getSchool().getId(), targetClass.getSchool().getId())) {
            throw new RuntimeException("Student and target class belong to different schools");
        }

        student.setSchoolClass(targetClass);
        return toStudentDto(studentRepository.save(student));
    }

    public AdminParentChildViewDTO linkParentToStudent(AdminParentChildLinkDTO dto) {
        if (dto == null || dto.getParentId() == null || dto.getStudentId() == null) {
            throw new RuntimeException("parentId and studentId are required");
        }

        Parent parent = parentRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (parentStudentRepository.existsByParentIdAndStudentId(parent.getId(), student.getId())) {
            throw new RuntimeException("This parent is already linked to the student");
        }

        Long parentSchoolId = parent.getUser() != null && parent.getUser().getSchool() != null
                ? parent.getUser().getSchool().getId()
                : null;

        Long studentSchoolId = student.getUser() != null && student.getUser().getSchool() != null
                ? student.getUser().getSchool().getId()
                : null;

        if (parentSchoolId == null || studentSchoolId == null || !Objects.equals(parentSchoolId, studentSchoolId)) {
            throw new RuntimeException("Parent and student must belong to the same school");
        }

        ParentStudent link = new ParentStudent();
        link.setParent(parent);
        link.setStudent(student);

        ParentStudent saved = parentStudentRepository.save(link);
        return toParentChildDto(saved);
    }

    public void unlinkParentFromStudent(Long parentId, Long studentId) {
        ParentStudent link = parentStudentRepository.findByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new RuntimeException("Parent-child link not found"));

        parentStudentRepository.delete(link);
    }

    @Transactional(readOnly = true)
    public List<AdminParentChildViewDTO> getChildrenByParent(Long parentId) {
        return parentStudentRepository.findByParentId(parentId)
                .stream()
                .map(this::toParentChildDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminParentChildViewDTO> getParentsByStudent(Long studentId) {
        return parentStudentRepository.findByStudentId(studentId)
                .stream()
                .map(this::toParentChildDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminParentChildViewDTO> getParentChildLinksBySchool(Long schoolId) {
        return parentRepository.findByUserSchoolIdWithUser(schoolId)
                .stream()
                .flatMap(parent -> parentStudentRepository.findByParentId(parent.getId()).stream())
                .map(this::toParentChildDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminParentChildViewDTO> getAvailableParentsBySchool(Long schoolId) {
        return parentRepository.findByUserSchoolIdWithUser(schoolId)
                .stream()
                .map(parent -> {
                    AdminParentChildViewDTO dto = new AdminParentChildViewDTO();
                    dto.setParentId(parent.getId());
                    dto.setParentName(buildFullName(parent.getUser()));
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
            throw new RuntimeException("schoolId is required");
        }
        if (dto.getName() == null || dto.getName().trim().isBlank()) {
            throw new RuntimeException("Class name is required");
        }
        if (dto.getAcademicYear() == null || dto.getAcademicYear().trim().isBlank()) {
            throw new RuntimeException("Academic year is required");
        }
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
        return dto;
    }

    private AdminStudentInClassDTO toStudentDto(Student student) {
        AdminStudentInClassDTO dto = new AdminStudentInClassDTO();
        dto.setStudentId(student.getId());

        if (student.getUser() != null) {
            dto.setUserId(student.getUser().getId());
            dto.setFullName(buildFullName(student.getUser()));
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

    private AdminParentChildViewDTO toParentChildDto(ParentStudent link) {
        AdminParentChildViewDTO dto = new AdminParentChildViewDTO();

        if (link.getParent() != null) {
            dto.setParentId(link.getParent().getId());
            if (link.getParent().getUser() != null) {
                dto.setParentName(buildFullName(link.getParent().getUser()));
                dto.setParentEmail(link.getParent().getUser().getEmail());
            }
        }

        if (link.getStudent() != null) {
            dto.setStudentId(link.getStudent().getId());
            if (link.getStudent().getUser() != null) {
                dto.setStudentName(buildFullName(link.getStudent().getUser()));
                dto.setStudentEmail(link.getStudent().getUser().getEmail());
            }
            if (link.getStudent().getSchoolClass() != null) {
                dto.setClassId(link.getStudent().getSchoolClass().getId());
                dto.setClassName(link.getStudent().getSchoolClass().getName());
            }
        }

        return dto;
    }

    private String buildFullName(User user) {
        if (user == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        if (user.getLastName() != null && !user.getLastName().isBlank()) {
            sb.append(user.getLastName().trim());
        }
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(user.getFirstName().trim());
        }
        if (user.getPatronymic() != null && !user.getPatronymic().isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(user.getPatronymic().trim());
        }
        return sb.toString();
    }
}