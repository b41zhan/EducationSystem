package nba.studix.userservice.Repository;

import nba.studix.userservice.Entity.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    @Query("SELECT ps FROM ParentStudent ps WHERE ps.parentId = :parentId")
    List<ParentStudent> findByParentId(@Param("parentId") Long parentId);

    @Query("SELECT ps FROM ParentStudent ps WHERE ps.studentId = :studentId")
    List<ParentStudent> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT ps.studentId FROM ParentStudent ps WHERE ps.parentId = :parentId")
    List<Long> findStudentIdsByParentId(@Param("parentId") Long parentId);

    @Query("SELECT ps.parentId FROM ParentStudent ps WHERE ps.studentId = :studentId")
    List<Long> findParentIdsByStudentId(@Param("studentId") Long studentId);
}