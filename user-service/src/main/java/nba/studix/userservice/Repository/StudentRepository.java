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
    @Query("SELECT s FROM Student s WHERE s.classId = :classId")
    List<Student> findByClassId(@Param("classId") Long classId);

    Optional<Student> findById(Long id);

    @Query("SELECT s FROM Student s WHERE s.id IN :studentIds")
    List<Student> findByIds(@Param("studentIds") List<Long> studentIds);
}
