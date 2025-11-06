package nba.studix.userservice.Repository;

import nba.studix.userservice.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);

    @Query("SELECT s FROM Student s WHERE s.schoolClass.id = :classId")
    List<Student> findByClassId(@Param("classId") Long classId);

    @Query("SELECT s FROM Student s WHERE s.schoolClass.id IN :classIds")
    List<Student> findByClassIds(@Param("classIds") List<Long> classIds);

    boolean existsByUserId(Long userId);
}