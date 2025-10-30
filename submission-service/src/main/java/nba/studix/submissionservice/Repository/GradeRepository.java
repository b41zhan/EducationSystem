package nba.studix.submissionservice.Repository;

import nba.studix.submissionservice.Entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("SELECT g FROM Grade g WHERE g.submissionId = :submissionId")
    Optional<Grade> findBySubmissionId(@Param("submissionId") Long submissionId);

    @Query("SELECT g FROM Grade g WHERE g.teacherId = :teacherId ORDER BY g.gradedAt DESC")
    List<Grade> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT g FROM Grade g WHERE g.submissionId IN :submissionIds")
    List<Grade> findBySubmissionIds(@Param("submissionIds") List<Long> submissionIds);
}