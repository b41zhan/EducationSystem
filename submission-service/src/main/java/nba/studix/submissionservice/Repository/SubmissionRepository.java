package nba.studix.submissionservice.Repository;

import nba.studix.submissionservice.Entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    @Query("SELECT s FROM Submission s WHERE s.studentId = :studentId ORDER BY s.submittedAt DESC")
    List<Submission> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT s FROM Submission s WHERE s.assignmentId = :assignmentId ORDER BY s.submittedAt DESC")
    List<Submission> findByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("SELECT s FROM Submission s WHERE s.assignmentId = :assignmentId AND s.studentId = :studentId")
    Optional<Submission> findByAssignmentIdAndStudentId(@Param("assignmentId") Long assignmentId,
                                                        @Param("studentId") Long studentId);

    @Query("SELECT s FROM Submission s WHERE s.status = :status ORDER BY s.submittedAt DESC")
    List<Submission> findByStatus(@Param("status") String status);

    @Query("SELECT s FROM Submission s WHERE s.assignmentId IN :assignmentIds ORDER BY s.submittedAt DESC")
    List<Submission> findByAssignmentIds(@Param("assignmentIds") List<Long> assignmentIds);
}