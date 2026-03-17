//package com.springdemo.educationsystem.Repository;
//
//import com.springdemo.educationsystem.Entity.QuizAssignment;
//import com.springdemo.educationsystem.Entity.QuizAttempt;
//import com.springdemo.educationsystem.Enum.QuizAttemptStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface QuizAssignmentRepository extends JpaRepository<QuizAssignment, Long> {
//    List<QuizAssignment> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
//
//    List<QuizAssignment> findBySchoolClassIdAndActiveTrueOrderByStartTimeDesc(Long classId);
//
//    List<QuizAssignment> findBySchoolClassIdAndActiveTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByStartTimeDesc(
//            Long classId,
//            LocalDateTime now1,
//            LocalDateTime now2
//    );
//    List<QuizAssignment> findByQuizId(Long quizId);
//
//    long countByQuizAssignmentIdAndStatus(Long assignmentId, QuizAttemptStatus status);
//}


//package com.springdemo.educationsystem.Repository;
//
//import com.springdemo.educationsystem.Entity.QuizAssignment;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface QuizAssignmentRepository extends JpaRepository<QuizAssignment, Long> {
//
//    List<QuizAssignment> findByQuizId(Long quizId);
//
//}

package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.QuizAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizAssignmentRepository extends JpaRepository<QuizAssignment, Long> {

    List<QuizAssignment> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    List<QuizAssignment> findBySchoolClassIdAndActiveTrueOrderByStartTimeDesc(Long classId);

    List<QuizAssignment> findByAssignedStudentsStudentIdAndActiveTrueOrderByStartTimeDesc(Long studentId);

    List<QuizAssignment> findBySchoolClassIdAndActiveTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByStartTimeDesc(
            Long classId,
            LocalDateTime now1,
            LocalDateTime now2
    );

    List<QuizAssignment> findByAssignedStudentsStudentIdAndActiveTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByStartTimeDesc(
            Long studentId,
            LocalDateTime now1,
            LocalDateTime now2
    );

    List<QuizAssignment> findByQuizId(Long quizId);
}