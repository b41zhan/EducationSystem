package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.*;
import com.springdemo.educationsystem.Entity.*;
import com.springdemo.educationsystem.Entity.AttendanceStatus;
import com.springdemo.educationsystem.Entity.JournalEntryType;
import com.springdemo.educationsystem.Enum.QuizAttemptStatus;
import com.springdemo.educationsystem.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherJournalService {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final AssignmentRepository assignmentRepository;
    private final GradeRepository gradeRepository;
    private final QuizAssignmentRepository quizAssignmentRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AttendanceMarkRepository attendanceMarkRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalFinalGradeRepository journalFinalGradeRepository;

    public TeacherJournalService(
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            SchoolClassRepository schoolClassRepository,
            SubjectRepository subjectRepository,
            AssignmentRepository assignmentRepository,
            GradeRepository gradeRepository,
            QuizAssignmentRepository quizAssignmentRepository,
            QuizAttemptRepository quizAttemptRepository,
            AttendanceMarkRepository attendanceMarkRepository,
            JournalEntryRepository journalEntryRepository,
            JournalFinalGradeRepository journalFinalGradeRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.assignmentRepository = assignmentRepository;
        this.gradeRepository = gradeRepository;
        this.quizAssignmentRepository = quizAssignmentRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.attendanceMarkRepository = attendanceMarkRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.journalFinalGradeRepository = journalFinalGradeRepository;
    }

    public List<TeacherJournalClassDTO> getTeacherJournalClasses(Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        List<Assignment> assignments = assignmentRepository.findAll()
                .stream()
                .filter(a -> a.getTeacher() != null && Objects.equals(a.getTeacher().getId(), teacher.getId()))
                .toList();

        Map<String, TeacherJournalClassDTO> unique = new LinkedHashMap<>();
        for (Assignment a : assignments) {
            if (a.getSchoolClass() == null || a.getSubject() == null) continue;

            String key = a.getSchoolClass().getId() + "_" + a.getSubject().getId();
            unique.putIfAbsent(
                    key,
                    new TeacherJournalClassDTO(
                            a.getSchoolClass().getId(),
                            a.getSchoolClass().getName(),
                            a.getSubject().getId(),
                            a.getSubject().getName()
                    )
            );
        }

        return new ArrayList<>(unique.values());
    }

    @Transactional(readOnly = true)
    public List<StudentJournalSubjectDTO> getStudentJournal(Long studentUserId, Integer quarter) {
        Student student = studentRepository.findById(studentUserId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getSchoolClass() == null) {
            return new ArrayList<>();
        }

        Long classId = student.getSchoolClass().getId();
        List<LocalDate> lessonDates = getQuarterDates(quarter);
        List<String> dateStrings = lessonDates.stream().map(LocalDate::toString).toList();

        List<JournalEntry> allEntries = journalEntryRepository.findAll()
                .stream()
                .filter(e -> e.getStudent() != null && Objects.equals(e.getStudent().getId(), student.getId()))
                .filter(e -> e.getSchoolClass() != null && Objects.equals(e.getSchoolClass().getId(), classId))
                .filter(e -> Objects.equals(e.getQuarter(), quarter))
                .toList();

        List<AttendanceMark> allAttendance = attendanceMarkRepository.findAll()
                .stream()
                .filter(a -> a.getStudent() != null && Objects.equals(a.getStudent().getId(), student.getId()))
                .filter(a -> a.getSchoolClass() != null && Objects.equals(a.getSchoolClass().getId(), classId))
                .filter(a -> Objects.equals(a.getQuarter(), quarter))
                .toList();

        List<JournalFinalGrade> allFinals = journalFinalGradeRepository.findAll()
                .stream()
                .filter(f -> f.getStudent() != null && Objects.equals(f.getStudent().getId(), student.getId()))
                .filter(f -> f.getSchoolClass() != null && Objects.equals(f.getSchoolClass().getId(), classId))
                .filter(f -> Objects.equals(f.getQuarter(), quarter))
                .toList();

        Map<Long, List<JournalEntry>> bySubjectEntries = allEntries.stream()
                .filter(e -> e.getSubject() != null)
                .collect(Collectors.groupingBy(e -> e.getSubject().getId(), LinkedHashMap::new, Collectors.toList()));

        Map<Long, List<AttendanceMark>> bySubjectAttendance = allAttendance.stream()
                .filter(a -> a.getSubject() != null)
                .collect(Collectors.groupingBy(a -> a.getSubject().getId(), LinkedHashMap::new, Collectors.toList()));

        Map<Long, JournalFinalGrade> bySubjectFinal = allFinals.stream()
                .filter(f -> f.getSubject() != null)
                .collect(Collectors.toMap(f -> f.getSubject().getId(), f -> f, (a, b) -> a, LinkedHashMap::new));

        Set<Long> subjectIds = new LinkedHashSet<>();
        subjectIds.addAll(bySubjectEntries.keySet());
        subjectIds.addAll(bySubjectAttendance.keySet());
        subjectIds.addAll(bySubjectFinal.keySet());

        List<StudentJournalSubjectDTO> result = new ArrayList<>();

        for (Long subjectId : subjectIds) {
            Subject subject = subjectRepository.findById(subjectId).orElse(null);
            if (subject == null) continue;

            StudentJournalSubjectDTO dto = new StudentJournalSubjectDTO();
            dto.setSubjectId(subject.getId());
            dto.setSubjectName(subject.getName());
            dto.setDates(dateStrings);

            Map<String, AttendanceMark> attendanceMap = new HashMap<>();
            for (AttendanceMark a : bySubjectAttendance.getOrDefault(subjectId, new ArrayList<>())) {
                attendanceMap.put(a.getLessonDate().toString(), a);
            }

            Map<String, List<JournalEntry>> entryMap = new HashMap<>();
            for (JournalEntry entry : bySubjectEntries.getOrDefault(subjectId, new ArrayList<>())) {
                entryMap.computeIfAbsent(entry.getLessonDate().toString(), k -> new ArrayList<>()).add(entry);
            }

            List<StudentJournalSubjectDTO.DayCellDTO> cells = new ArrayList<>();
            for (LocalDate date : lessonDates) {
                StudentJournalSubjectDTO.DayCellDTO cell = new StudentJournalSubjectDTO.DayCellDTO();
                cell.setDate(date.toString());

                AttendanceMark mark = attendanceMap.get(date.toString());
                if (mark != null && mark.getStatus() != AttendanceStatus.PRESENT) {
                    cell.setAttendanceCode(attendanceCode(mark.getStatus()));
                    cell.setAttendanceColor(attendanceColor(mark.getStatus()));
                }

                List<JournalEntry> dayEntries = new ArrayList<>(entryMap.getOrDefault(date.toString(), new ArrayList<>()));

                List<StudentJournalSubjectDTO.EntryDTO> items = new ArrayList<>();
                for (JournalEntry e : dayEntries) {
                    StudentJournalSubjectDTO.EntryDTO item = new StudentJournalSubjectDTO.EntryDTO();
                    item.setType(e.getEntryType().name());
                    item.setLabel(labelForEntryType(e.getEntryType()));
                    item.setDisplayValue(e.getDisplayValue());
                    item.setNumericValue(e.getNumericValue());
                    items.add(item);
                }

                cell.setEntries(pickLastThreeTypes(items));
                cells.add(cell);
            }

            dto.setCells(cells);

            JournalFinalGrade finalGrade = bySubjectFinal.get(subjectId);
            if (finalGrade != null) {
                StudentJournalSubjectDTO.FinalDTO fd = new StudentJournalSubjectDTO.FinalDTO();
                fd.setQuarterGrade(finalGrade.getQuarterGrade());
                fd.setCalculatedQuarterGrade(finalGrade.getCalculatedQuarterGrade());
                fd.setYearGrade(finalGrade.getYearGrade());
                fd.setCalculatedYearGrade(finalGrade.getCalculatedYearGrade());
                dto.setFinalGrade(fd);
            }

            result.add(dto);
        }

        result.sort(Comparator.comparing(StudentJournalSubjectDTO::getSubjectName));
        return result;
    }

    private List<StudentJournalSubjectDTO.EntryDTO> pickLastThreeTypes(List<StudentJournalSubjectDTO.EntryDTO> items) {
        StudentJournalSubjectDTO.EntryDTO lesson = null;
        StudentJournalSubjectDTO.EntryDTO assignment = null;
        StudentJournalSubjectDTO.EntryDTO quiz = null;

        for (StudentJournalSubjectDTO.EntryDTO item : items) {
            if ("LESSON_GRADE".equals(item.getType())) {
                lesson = item;
            } else if ("ASSIGNMENT_GRADE".equals(item.getType())) {
                assignment = item;
            } else if ("QUIZ_GRADE".equals(item.getType())) {
                quiz = item;
            }
        }

        List<StudentJournalSubjectDTO.EntryDTO> result = new ArrayList<>();
        if (quiz != null) result.add(quiz);
        if (assignment != null) result.add(assignment);
        if (lesson != null) result.add(lesson);
        return result;
    }





    @Transactional
    public TeacherJournalDTO getJournal(Long teacherUserId, Long classId, Long subjectId, Integer quarter) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        syncAssignmentsIntoJournal(teacher, classId, subjectId, quarter);
        syncQuizzesIntoJournal(teacher, classId, subjectId, quarter);

        List<LocalDate> lessonDates = getQuarterDates(quarter);
        List<String> dateStrings = lessonDates.stream().map(LocalDate::toString).toList();

        List<Student> students = studentRepository.findBySchoolClassIdWithUser(classId);
        students.sort(Comparator.comparing(s -> buildStudentName(s.getUser())));

        List<AttendanceMark> attendanceList =
                attendanceMarkRepository.findByTeacherIdAndSchoolClassIdAndSubjectIdAndQuarterOrderByLessonDateAsc(
                        teacher.getId(), classId, subjectId, quarter
                );

        List<JournalEntry> entryList =
                journalEntryRepository.findByTeacherIdAndSchoolClassIdAndSubjectIdAndQuarterOrderByLessonDateAsc(
                        teacher.getId(), classId, subjectId, quarter
                );

        List<JournalFinalGrade> finals =
                journalFinalGradeRepository.findByTeacherIdAndSchoolClassIdAndSubjectIdAndQuarter(
                        teacher.getId(), classId, subjectId, quarter
                );

        Map<String, AttendanceMark> attendanceMap = new HashMap<>();
        for (AttendanceMark a : attendanceList) {
            attendanceMap.put(a.getStudent().getId() + "_" + a.getLessonDate(), a);
        }

        Map<String, List<JournalEntry>> entryMap = new HashMap<>();
        for (JournalEntry entry : entryList) {
            String key = entry.getStudent().getId() + "_" + entry.getLessonDate();
            entryMap.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
        }

        Map<Long, JournalFinalGrade> finalMap = finals.stream()
                .collect(Collectors.toMap(f -> f.getStudent().getId(), f -> f, (a, b) -> a));

        TeacherJournalDTO dto = new TeacherJournalDTO();
        dto.setClassId(schoolClass.getId());
        dto.setClassName(schoolClass.getName());
        dto.setSubjectId(subject.getId());
        dto.setSubjectName(subject.getName());
        dto.setQuarter(quarter);
        dto.setDates(dateStrings);

        List<TeacherJournalDTO.StudentRowDTO> rows = new ArrayList<>();

        for (Student student : students) {
            TeacherJournalDTO.StudentRowDTO row = new TeacherJournalDTO.StudentRowDTO();
            row.setStudentId(student.getId());
            row.setStudentName(buildStudentName(student.getUser()));

            List<TeacherJournalDTO.DayCellDTO> cells = new ArrayList<>();
            for (LocalDate date : lessonDates) {
                TeacherJournalDTO.DayCellDTO cell = new TeacherJournalDTO.DayCellDTO();
                cell.setDate(date.toString());

                AttendanceMark mark = attendanceMap.get(student.getId() + "_" + date);
                if (mark != null && mark.getStatus() != AttendanceStatus.PRESENT) {
                    cell.setAttendanceCode(attendanceCode(mark.getStatus()));
                    cell.setAttendanceColor(attendanceColor(mark.getStatus()));
                }

                List<JournalEntry> dayEntries = new ArrayList<>(entryMap.getOrDefault(student.getId() + "_" + date, new ArrayList<>()));
                dayEntries.sort(Comparator.comparing(e -> e.getEntryType().name()));

                List<TeacherJournalDTO.EntryDTO> entryDtos = new ArrayList<>();
                for (JournalEntry e : dayEntries) {
                    TeacherJournalDTO.EntryDTO item = new TeacherJournalDTO.EntryDTO();
                    item.setId(e.getId());
                    item.setType(e.getEntryType().name());
                    item.setLabel(labelForEntryType(e.getEntryType()));
                    item.setDisplayValue(e.getDisplayValue());
                    item.setNumericValue(e.getNumericValue());
                    item.setSourceId(e.getSourceId());
                    item.setSourceType(e.getSourceType());
                    item.setEditable(e.getEntryType() == JournalEntryType.LESSON_GRADE);
                    entryDtos.add(item);
                }

                cell.setEntries(entryDtos);
                cells.add(cell);
            }

            row.setCells(cells);

            JournalFinalGrade fg = finalMap.get(student.getId());
            TeacherJournalDTO.FinalDTO finalDto = new TeacherJournalDTO.FinalDTO();
            if (fg != null) {
                finalDto.setQuarterGrade(fg.getQuarterGrade());
                finalDto.setCalculatedQuarterGrade(fg.getCalculatedQuarterGrade());
                finalDto.setYearGrade(fg.getYearGrade());
                finalDto.setCalculatedYearGrade(fg.getCalculatedYearGrade());
                finalDto.setQuarterManual(fg.isQuarterManual());
                finalDto.setYearManual(fg.isYearManual());
            }
            row.setFinalGrade(finalDto);

            rows.add(row);
        }

        dto.setStudents(rows);
        return dto;
    }

    @Transactional
    public void toggleAttendance(Long teacherUserId, UpsertAttendanceDTO request) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        LocalDate lessonDate = LocalDate.parse(request.getLessonDate());
        AttendanceStatus newStatus = AttendanceStatus.valueOf(request.getStatus());

        Optional<AttendanceMark> existingOpt = attendanceMarkRepository
                .findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndLessonDate(
                        teacher.getId(), student.getId(), schoolClass.getId(), subject.getId(), lessonDate
                );

        if (existingOpt.isPresent()) {
            AttendanceMark existing = existingOpt.get();

            if (existing.getStatus() == newStatus) {
                attendanceMarkRepository.delete(existing);
                return;
            }

            existing.setStatus(newStatus);
            existing.setUpdatedAt(LocalDateTime.now());
            attendanceMarkRepository.save(existing);
            return;
        }

        AttendanceMark mark = new AttendanceMark();
        mark.setTeacher(teacher);
        mark.setStudent(student);
        mark.setSchoolClass(schoolClass);
        mark.setSubject(subject);
        mark.setLessonDate(lessonDate);
        mark.setQuarter(request.getQuarter());
        mark.setStatus(newStatus);
        mark.setUpdatedAt(LocalDateTime.now());

        attendanceMarkRepository.save(mark);
    }

    @Transactional
    public void upsertLessonGrade(Long teacherUserId, UpsertLessonGradeDTO request) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        LocalDate lessonDate = LocalDate.parse(request.getLessonDate());

        Optional<JournalEntry> existingOpt = journalEntryRepository
                .findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndLessonDateAndEntryType(
                        teacher.getId(), student.getId(), schoolClass.getId(), subject.getId(), lessonDate, JournalEntryType.LESSON_GRADE
                );

        if (request.getValue() == null) {
            existingOpt.ifPresent(journalEntryRepository::delete);
            return;
        }

        JournalEntry entry = existingOpt.orElseGet(JournalEntry::new);

        entry.setTeacher(teacher);
        entry.setStudent(student);
        entry.setSchoolClass(schoolClass);
        entry.setSubject(subject);
        entry.setLessonDate(lessonDate);
        entry.setQuarter(request.getQuarter());
        entry.setEntryType(JournalEntryType.LESSON_GRADE);
        entry.setNumericValue(request.getValue());
        entry.setMaxValue(10.0);
        entry.setDisplayValue(trimZero(request.getValue()));
        entry.setSourceId(null);
        entry.setSourceType("LESSON");
        entry.setComment(request.getComment());
        entry.setManual(true);

        journalEntryRepository.save(entry);
    }

    @Transactional
    public void upsertQuarterFinalGrade(Long teacherUserId, UpsertFinalGradeDTO request) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        JournalFinalGrade grade = journalFinalGradeRepository
                .findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndQuarter(
                        teacher.getId(), student.getId(), schoolClass.getId(), subject.getId(), request.getQuarter()
                )
                .orElseGet(JournalFinalGrade::new);

        grade.setTeacher(teacher);
        grade.setStudent(student);
        grade.setSchoolClass(schoolClass);
        grade.setSubject(subject);
        grade.setQuarter(request.getQuarter());

        grade.setQuarterGrade(request.getQuarterGrade());
        grade.setQuarterManual(request.getQuarterGrade() != null);

        journalFinalGradeRepository.save(grade);
    }

    @Transactional
    public void upsertYearFinalGrade(Long teacherUserId, UpsertFinalGradeDTO request) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        JournalFinalGrade grade = journalFinalGradeRepository
                .findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndQuarter(
                        teacher.getId(), student.getId(), schoolClass.getId(), subject.getId(), request.getQuarter()
                )
                .orElseGet(JournalFinalGrade::new);

        grade.setTeacher(teacher);
        grade.setStudent(student);
        grade.setSchoolClass(schoolClass);
        grade.setSubject(subject);
        grade.setQuarter(request.getQuarter());

        grade.setYearGrade(request.getYearGrade());
        grade.setYearManual(request.getYearGrade() != null);

        journalFinalGradeRepository.save(grade);
    }

    @Transactional
    public Map<String, Object> calculateQuarterFinal(Long teacherUserId, Long classId, Long subjectId, Long studentId, Integer quarter) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        JournalFinalGrade fg = journalFinalGradeRepository
                .findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndQuarter(
                        teacher.getId(), studentId, classId, subjectId, quarter
                )
                .orElseGet(JournalFinalGrade::new);

        fg.setTeacher(teacher);
        fg.setStudent(student);
        fg.setSchoolClass(schoolClass);
        fg.setSubject(subject);
        fg.setQuarter(quarter);

        Integer quarterAverage = calculateQuarterAverageAsSchoolGrade(
                teacher.getId(), studentId, classId, subjectId, quarter
        );

        fg.setCalculatedQuarterGrade(quarterAverage == null ? null : quarterAverage.doubleValue());

        if (!fg.isQuarterManual()) {
            fg.setQuarterGrade(quarterAverage == null ? null : quarterAverage.doubleValue());
        }

        journalFinalGradeRepository.save(fg);

        Map<String, Object> result = new HashMap<>();
        result.put("quarterGrade", fg.getQuarterGrade());
        result.put("calculatedQuarterGrade", fg.getCalculatedQuarterGrade());
        return result;
    }

    @Transactional
    public Map<String, Object> calculateYearFinal(Long teacherUserId, Long classId, Long subjectId, Long studentId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        JournalFinalGrade q4 = journalFinalGradeRepository
                .findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndQuarter(
                        teacher.getId(), studentId, classId, subjectId, 4
                )
                .orElseGet(JournalFinalGrade::new);

        q4.setTeacher(teacher);
        q4.setStudent(student);
        q4.setSchoolClass(schoolClass);
        q4.setSubject(subject);
        q4.setQuarter(4);

        List<JournalFinalGrade> allQuarterRows = journalFinalGradeRepository
                .findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdOrderByQuarterAsc(
                        teacher.getId(), studentId, classId, subjectId
                );

        List<Double> values = allQuarterRows.stream()
                .filter(row -> row.getQuarter() != null && row.getQuarter() >= 1 && row.getQuarter() <= 4)
                .map(row -> row.getQuarterGrade() != null ? row.getQuarterGrade() : row.getCalculatedQuarterGrade())
                .filter(Objects::nonNull)
                .toList();

        Integer yearAverage = values.isEmpty()
                ? null
                : (int) Math.round(values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));

        q4.setCalculatedYearGrade(yearAverage == null ? null : yearAverage.doubleValue());

        if (!q4.isYearManual()) {
            q4.setYearGrade(yearAverage == null ? null : yearAverage.doubleValue());
        }

        journalFinalGradeRepository.save(q4);

        Map<String, Object> result = new HashMap<>();
        result.put("yearGrade", q4.getYearGrade());
        result.put("calculatedYearGrade", q4.getCalculatedYearGrade());
        return result;
    }

    @Transactional
    public void syncAssignments(Long teacherUserId, Long classId, Long subjectId, Integer quarter) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        syncAssignmentsIntoJournal(teacher, classId, subjectId, quarter);
    }

    @Transactional
    public void syncQuizzes(Long teacherUserId, Long classId, Long subjectId, Integer quarter) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        syncQuizzesIntoJournal(teacher, classId, subjectId, quarter);
    }

    private void syncAssignmentsIntoJournal(Teacher teacher, Long classId, Long subjectId, Integer quarter) {
        List<Grade> grades = gradeRepository.findByTeacherIdAndClassId(teacher.getId(), classId);

        LocalDate start = quarterStart(quarter);
        LocalDate end = quarterEnd(quarter);

        for (Grade g : grades) {
            if (g.getSubmission() == null || g.getSubmission().getAssignment() == null) continue;

            Submission submission = g.getSubmission();
            Assignment assignment = submission.getAssignment();

            if (assignment.getSubject() == null || !Objects.equals(assignment.getSubject().getId(), subjectId)) continue;
            if (assignment.getSchoolClass() == null || !Objects.equals(assignment.getSchoolClass().getId(), classId)) continue;
            if (submission.getStudent() == null) continue;
            if (g.getGradeValue() == null) continue;
            if (assignment.getDeadline() == null) continue;

            LocalDate lessonDate = assignment.getDeadline().toLocalDate();
            if (lessonDate.isBefore(start) || lessonDate.isAfter(end)) continue;

            Double normalized = assignment.getMaxGrade() == null || assignment.getMaxGrade() == 0
                    ? null
                    : (g.getGradeValue() * 10.0) / assignment.getMaxGrade();

            JournalEntry entry = journalEntryRepository
                    .findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndLessonDateAndEntryTypeAndSourceId(
                            teacher.getId(),
                            submission.getStudent().getId(),
                            classId,
                            subjectId,
                            lessonDate,
                            JournalEntryType.ASSIGNMENT_GRADE,
                            assignment.getId()
                    )
                    .orElseGet(JournalEntry::new);

            entry.setTeacher(teacher);
            entry.setStudent(submission.getStudent());
            entry.setSchoolClass(assignment.getSchoolClass());
            entry.setSubject(assignment.getSubject());
            entry.setLessonDate(lessonDate);
            entry.setQuarter(quarter);
            entry.setEntryType(JournalEntryType.ASSIGNMENT_GRADE);
            entry.setNumericValue(normalized);
            entry.setMaxValue(10.0);
            entry.setDisplayValue(g.getGradeValue() + "/" + assignment.getMaxGrade());
            entry.setSourceId(assignment.getId());
            entry.setSourceType("ASSIGNMENT");
            entry.setComment(g.getComment());
            entry.setManual(false);

            journalEntryRepository.save(entry);
        }
    }

    private void syncQuizzesIntoJournal(Teacher teacher, Long classId, Long subjectId, Integer quarter) {
        List<QuizAssignment> assignments = quizAssignmentRepository.findByTeacherIdOrderByCreatedAtDesc(teacher.getId());

        LocalDate start = quarterStart(quarter);
        LocalDate end = quarterEnd(quarter);

        System.out.println("=== QUIZ SYNC START ===");
        System.out.println("teacherId=" + teacher.getId());
        System.out.println("classId=" + classId);
        System.out.println("subjectId=" + subjectId);
        System.out.println("quarter=" + quarter);
        System.out.println("range=" + start + " .. " + end);
        System.out.println("quizAssignments count=" + assignments.size());

        for (QuizAssignment qa : assignments) {
            System.out.println("\n--- CHECK QUIZ ASSIGNMENT id=" + qa.getId() + " ---");

            if (qa.getSchoolClass() == null) {
                System.out.println("SKIP: schoolClass is null");
                continue;
            }

            System.out.println("qa.classId=" + qa.getSchoolClass().getId());

            if (!Objects.equals(qa.getSchoolClass().getId(), classId)) {
                System.out.println("SKIP: class mismatch");
                continue;
            }

            if (qa.getQuiz() == null) {
                System.out.println("SKIP: quiz is null");
                continue;
            }

            System.out.println("qa.quizId=" + qa.getQuiz().getId());

            if (qa.getQuiz().getSubject() == null) {
                System.out.println("SKIP: quiz.subject is null");
                continue;
            }

            System.out.println("qa.quiz.subjectId=" + qa.getQuiz().getSubject().getId());

            if (!Objects.equals(qa.getQuiz().getSubject().getId(), subjectId)) {
                System.out.println("SKIP: subject mismatch");
                continue;
            }

            LocalDate lessonDate;
            if (qa.getEndTime() != null) {
                lessonDate = qa.getEndTime().toLocalDate();
                System.out.println("lessonDate from endTime=" + lessonDate);
            } else if (qa.getStartTime() != null) {
                lessonDate = qa.getStartTime().toLocalDate();
                System.out.println("lessonDate from startTime=" + lessonDate);
            } else {
                System.out.println("SKIP: both startTime and endTime are null");
                continue;
            }

            if (lessonDate.isBefore(start) || lessonDate.isAfter(end)) {
                System.out.println("SKIP: lessonDate out of quarter range");
                continue;
            }

            List<QuizAttempt> attempts = quizAttemptRepository.findByQuizAssignmentIdOrderByStartTimeDesc(qa.getId());
            System.out.println("attempts count=" + (attempts == null ? 0 : attempts.size()));

            if (attempts == null || attempts.isEmpty()) {
                System.out.println("SKIP: no attempts");
                continue;
            }

            int totalPoints = qa.getQuiz().getQuestions() == null
                    ? 0
                    : qa.getQuiz().getQuestions().stream()
                    .map(QuizQuestion::getPoints)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            System.out.println("totalPoints=" + totalPoints);

            for (QuizAttempt attempt : attempts) {
                System.out.println("  attemptId=" + attempt.getId()
                        + ", studentId=" + (attempt.getStudent() != null ? attempt.getStudent().getId() : null)
                        + ", score=" + attempt.getScore()
                        + ", status=" + attempt.getStatus());

                if (attempt.getStudent() == null) {
                    System.out.println("  SKIP ATTEMPT: student is null");
                    continue;
                }

                if (attempt.getScore() == null) {
                    System.out.println("  SKIP ATTEMPT: score is null");
                    continue;
                }

                if (attempt.getStatus() != QuizAttemptStatus.SUBMITTED
                        && attempt.getStatus() != QuizAttemptStatus.TIME_EXPIRED) {
                    System.out.println("  SKIP ATTEMPT: status is not SUBMITTED/TIME_EXPIRED");
                    continue;
                }

                Double normalized = totalPoints == 0 ? null : (attempt.getScore() * 10.0) / totalPoints;

                JournalEntry entry = journalEntryRepository
                        .findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndLessonDateAndEntryTypeAndSourceId(
                                teacher.getId(),
                                attempt.getStudent().getId(),
                                classId,
                                subjectId,
                                lessonDate,
                                JournalEntryType.QUIZ_GRADE,
                                qa.getId()
                        )
                        .orElseGet(JournalEntry::new);

                entry.setTeacher(teacher);
                entry.setStudent(attempt.getStudent());
                entry.setSchoolClass(qa.getSchoolClass());
                entry.setSubject(qa.getQuiz().getSubject());
                entry.setLessonDate(lessonDate);
                entry.setQuarter(quarter);
                entry.setEntryType(JournalEntryType.QUIZ_GRADE);
                entry.setNumericValue(normalized);
                entry.setMaxValue(10.0);
                entry.setDisplayValue(attempt.getScore() + "/" + totalPoints);
                entry.setSourceId(qa.getId());
                entry.setSourceType("QUIZ");
                entry.setComment(null);
                entry.setManual(false);

                journalEntryRepository.save(entry);
                System.out.println("  SAVED QUIZ ENTRY: " + entry.getDisplayValue());
            }
        }

        System.out.println("=== QUIZ SYNC END ===");
    }

    private Integer calculateQuarterAverageAsSchoolGrade(Long teacherId, Long studentId, Long classId, Long subjectId, Integer quarter) {
        List<JournalEntry> entries = journalEntryRepository.findByTeacherIdAndStudentIdAndSchoolClassIdAndSubjectIdAndQuarter(
                teacherId, studentId, classId, subjectId, quarter
        );

        List<Double> values = entries.stream()
                .map(JournalEntry::getNumericValue)
                .filter(Objects::nonNull)
                .toList();

        if (values.isEmpty()) return null;

        double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return (int) Math.round(avg);
    }

    private String buildStudentName(User user) {
        if (user == null) return "Без имени";
        return (safe(user.getLastName()) + " " + safe(user.getFirstName())).trim();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String attendanceCode(AttendanceStatus status) {
        return switch (status) {
            case EXCUSED -> "П";
            case ABSENT -> "Н";
            case SICK -> "Б";
            case PRESENT -> "";
        };
    }

    private String attendanceColor(AttendanceStatus status) {
        return switch (status) {
            case EXCUSED -> "orange";
            case ABSENT -> "red";
            case SICK -> "yellow";
            case PRESENT -> "";
        };
    }

    private String labelForEntryType(JournalEntryType type) {
        return switch (type) {
            case LESSON_GRADE -> "Урок";
            case ASSIGNMENT_GRADE -> "Задание";
            case QUIZ_GRADE -> "Квиз";
        };
    }

    private int getAcademicYearStart() {
        LocalDate now = LocalDate.now();
        return now.getMonthValue() >= 9 ? now.getYear() : now.getYear() - 1;
    }

    private List<LocalDate> getQuarterDates(Integer quarter) {
        LocalDate start = quarterStart(quarter);
        LocalDate end = quarterEnd(quarter);

        List<LocalDate> dates = new ArrayList<>();
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            if (cur.getDayOfWeek() != DayOfWeek.SATURDAY && cur.getDayOfWeek() != DayOfWeek.SUNDAY) {
                dates.add(cur);
            }
            cur = cur.plusDays(1);
        }
        return dates;
    }

    private LocalDate quarterStart(Integer quarter) {
        int academicYearStart = getAcademicYearStart();

        return switch (quarter) {
            case 1 -> LocalDate.of(academicYearStart, 9, 2);
            case 2 -> LocalDate.of(academicYearStart, 11, 4);
            case 3 -> LocalDate.of(academicYearStart + 1, 1, 9);
            case 4 -> LocalDate.of(academicYearStart + 1, 4, 1);
            default -> throw new RuntimeException("Invalid quarter");
        };
    }

    private LocalDate quarterEnd(Integer quarter) {
        int academicYearStart = getAcademicYearStart();

        return switch (quarter) {
            case 1 -> LocalDate.of(academicYearStart, 10, 27);
            case 2 -> LocalDate.of(academicYearStart, 12, 29);
            case 3 -> LocalDate.of(academicYearStart + 1, 3, 20);
            case 4 -> LocalDate.of(academicYearStart + 1, 5, 25);
            default -> throw new RuntimeException("Invalid quarter");
        };
    }

    private String trimZero(Double value) {
        if (value == null) return "";
        if (value == Math.floor(value)) {
            return String.valueOf(value.intValue());
        }
        return String.valueOf(value);
    }
}