package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.AdminTeacherClassSubjectCreateDTO;
import com.springdemo.educationsystem.DTO.AdminTeacherClassSubjectDTO;
import com.springdemo.educationsystem.DTO.TeacherClassSubjectPairDTO;
import com.springdemo.educationsystem.DTO.TeacherSimpleOptionDTO;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherClassSubjectService {

    private final TeacherClassSubjectRepository teacherClassSubjectRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final QuizRepository quizRepository;

    public TeacherClassSubjectService(TeacherClassSubjectRepository teacherClassSubjectRepository,
                                      TeacherRepository teacherRepository,
                                      SchoolClassRepository schoolClassRepository,
                                      SubjectRepository subjectRepository,
                                      QuizRepository quizRepository) {
        this.teacherClassSubjectRepository = teacherClassSubjectRepository;
        this.teacherRepository = teacherRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.quizRepository = quizRepository;
    }

    public List<AdminTeacherClassSubjectDTO> getAssignmentsBySchool(Long schoolId) {
        return teacherClassSubjectRepository.findBySchoolIdWithRelations(schoolId)
                .stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());
    }

    public List<TeacherSimpleOptionDTO> getTeachersBySchool(Long schoolId) {
        return teacherRepository.findByUserSchoolIdWithUser(schoolId)
                .stream()
                .map(this::toTeacherOptionDto)
                .collect(Collectors.toList());
    }

    public AdminTeacherClassSubjectDTO createAssignment(AdminTeacherClassSubjectCreateDTO dto) {
        if (dto == null || dto.getTeacherId() == null || dto.getClassId() == null || dto.getSubjectId() == null) {
            throw new RuntimeException("teacherId, classId and subjectId are required");
        }

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        SchoolClass schoolClass = schoolClassRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (teacher.getUser() == null || teacher.getUser().getSchool() == null) {
            throw new RuntimeException("Teacher is not attached to a school");
        }

        if (schoolClass.getSchool() == null) {
            throw new RuntimeException("Class is not attached to a school");
        }

        if (!Objects.equals(teacher.getUser().getSchool().getId(), schoolClass.getSchool().getId())) {
            throw new RuntimeException("Teacher and class must belong to the same school");
        }

        boolean exists = teacherClassSubjectRepository.existsByTeacherIdAndSchoolClassIdAndSubjectIdAndActiveTrue(
                teacher.getId(), schoolClass.getId(), subject.getId()
        );

        if (exists) {
            throw new RuntimeException("This teaching assignment already exists");
        }

        TeacherClassSubject entity = new TeacherClassSubject();
        entity.setTeacher(teacher);
        entity.setSchoolClass(schoolClass);
        entity.setSubject(subject);
        entity.setActive(true);

        return toAdminDto(teacherClassSubjectRepository.save(entity));
    }

    public void deactivateAssignment(Long assignmentId) {
        TeacherClassSubject entity = teacherClassSubjectRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Teaching assignment not found"));

        entity.setActive(false);
        teacherClassSubjectRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<TeacherClassSubjectPairDTO> getTeacherPairs(Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return teacherClassSubjectRepository.findActiveByTeacherIdWithRelations(teacher.getId())
                .stream()
                .map(this::toPairDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SchoolClass> getTeacherClasses(Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return teacherClassSubjectRepository.findDistinctActiveClassesByTeacherId(teacher.getId());
    }

    @Transactional(readOnly = true)
    public List<Subject> getTeacherSubjects(Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return teacherClassSubjectRepository.findDistinctActiveSubjectsByTeacherId(teacher.getId());
    }

    @Transactional(readOnly = true)
    public void requireTeacherHasClassSubject(Long teacherUserId, Long classId, Long subjectId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        requireTeacherHasClassSubjectByTeacherEntityId(teacher.getId(), classId, subjectId);
    }

    @Transactional(readOnly = true)
    public void requireTeacherHasClassSubjectByTeacherEntityId(Long teacherId, Long classId, Long subjectId) {
        boolean allowed = teacherClassSubjectRepository.existsByTeacherIdAndSchoolClassIdAndSubjectIdAndActiveTrue(
                teacherId, classId, subjectId
        );

        if (!allowed) {
            throw new RuntimeException("Teacher is not assigned to this class-subject pair");
        }
    }

    @Transactional(readOnly = true)
    public void requireTeacherCanUseSubjectByTeacherEntityId(Long teacherId, Long subjectId) {
        if (subjectId == null) {
            throw new RuntimeException("subjectId is required");
        }

        boolean allowed = teacherClassSubjectRepository.existsByTeacherIdAndSubjectIdAndActiveTrue(teacherId, subjectId);

        if (!allowed) {
            throw new RuntimeException("Teacher is not assigned to this subject");
        }
    }

    @Transactional(readOnly = true)
    public void requireTeacherHasAnyAssignmentForClass(Long teacherUserId, Long classId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        boolean allowed = teacherClassSubjectRepository.existsByTeacherIdAndSchoolClassIdAndActiveTrue(
                teacher.getId(), classId
        );

        if (!allowed) {
            throw new RuntimeException("Teacher has no active teaching assignments in this class");
        }
    }

    @Transactional(readOnly = true)
    public void requireTeacherCanAssignQuizToClassByTeacherEntityId(Long teacherId, Long quizId, Long classId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (quiz.getTeacher() == null || !Objects.equals(quiz.getTeacher().getId(), teacherId)) {
            throw new RuntimeException("You can assign only your own quiz");
        }

        if (quiz.getSubject() == null) {
            boolean hasAny = teacherClassSubjectRepository.existsByTeacherIdAndSchoolClassIdAndActiveTrue(
                    teacherId, classId
            );

            if (!hasAny) {
                throw new RuntimeException("Teacher has no active teaching assignment in this class");
            }

            return;
        }

        requireTeacherHasClassSubjectByTeacherEntityId(teacherId, classId, quiz.getSubject().getId());
    }

    private AdminTeacherClassSubjectDTO toAdminDto(TeacherClassSubject entity) {
        AdminTeacherClassSubjectDTO dto = new AdminTeacherClassSubjectDTO();

        dto.setId(entity.getId());
        dto.setActive(entity.isActive());
        dto.setAssignedAt(entity.getAssignedAt());

        if (entity.getTeacher() != null) {
            dto.setTeacherId(entity.getTeacher().getId());

            if (entity.getTeacher().getUser() != null) {
                dto.setTeacherUserId(entity.getTeacher().getUser().getId());
                dto.setTeacherFullName(formatUserFullName(entity.getTeacher().getUser()));
                dto.setTeacherEmail(entity.getTeacher().getUser().getEmail());

                if (entity.getTeacher().getUser().getSchool() != null) {
                    dto.setSchoolId(entity.getTeacher().getUser().getSchool().getId());
                    dto.setSchoolName(entity.getTeacher().getUser().getSchool().getName());
                }
            }
        }

        if (entity.getSchoolClass() != null) {
            dto.setClassId(entity.getSchoolClass().getId());
            dto.setClassName(entity.getSchoolClass().getName());
            dto.setAcademicYear(entity.getSchoolClass().getAcademicYear());

            if (entity.getSchoolClass().getSchool() != null && dto.getSchoolId() == null) {
                dto.setSchoolId(entity.getSchoolClass().getSchool().getId());
                dto.setSchoolName(entity.getSchoolClass().getSchool().getName());
            }
        }

        if (entity.getSubject() != null) {
            dto.setSubjectId(entity.getSubject().getId());
            dto.setSubjectName(entity.getSubject().getName());
        }

        return dto;
    }

    private TeacherClassSubjectPairDTO toPairDto(TeacherClassSubject entity) {
        TeacherClassSubjectPairDTO dto = new TeacherClassSubjectPairDTO();
        dto.setAssignmentId(entity.getId());

        if (entity.getSchoolClass() != null) {
            dto.setClassId(entity.getSchoolClass().getId());
            dto.setClassName(entity.getSchoolClass().getName());
            dto.setAcademicYear(entity.getSchoolClass().getAcademicYear());
        }

        if (entity.getSubject() != null) {
            dto.setSubjectId(entity.getSubject().getId());
            dto.setSubjectName(entity.getSubject().getName());
        }

        return dto;
    }

    private TeacherSimpleOptionDTO toTeacherOptionDto(Teacher teacher) {
        TeacherSimpleOptionDTO dto = new TeacherSimpleOptionDTO();
        dto.setTeacherId(teacher.getId());

        if (teacher.getUser() != null) {
            dto.setUserId(teacher.getUser().getId());
            dto.setFullName(formatUserFullName(teacher.getUser()));
            dto.setEmail(teacher.getUser().getEmail());
        }

        return dto;
    }

    private String formatUserFullName(User user) {
        if (user == null) {
            return "—";
        }

        String result = String.join(" ",
                safe(user.getLastName()),
                safe(user.getFirstName()),
                safe(user.getPatronymic())
        ).trim();

        return result.replaceAll("\\s+", " ");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}