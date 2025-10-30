package nba.studix.assignmentservice.Repository;

import nba.studix.assignmentservice.Entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("SELECT a FROM Assignment a WHERE a.classId = :classId AND a.isActive = true")
    List<Assignment> findByClassId(@Param("classId") Long classId);

    @Query("SELECT a FROM Assignment a WHERE a.teacherId = :teacherId AND a.isActive = true")
    List<Assignment> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT a FROM Assignment a WHERE a.type = :type AND a.isActive = true")
    List<Assignment> findByType(@Param("type") String type);

    @Query("SELECT a FROM Assignment a WHERE a.subject.id = :subjectId AND a.isActive = true")
    List<Assignment> findBySubjectId(@Param("subjectId") Long subjectId);

    @Query("SELECT a FROM Assignment a WHERE a.isActive = true ORDER BY a.createdAt DESC")
    List<Assignment> findAllActive();

    @Query("SELECT a FROM Assignment a WHERE a.deadline < CURRENT_TIMESTAMP AND a.isActive = true")
    List<Assignment> findOverdueAssignments();
}