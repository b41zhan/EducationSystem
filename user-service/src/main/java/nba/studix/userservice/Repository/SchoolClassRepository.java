package nba.studix.userservice.Repository;

import nba.studix.userservice.Entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    @Query("SELECT sc FROM SchoolClass sc WHERE sc.school.id = :schoolId")
    List<SchoolClass> findBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT sc FROM SchoolClass sc WHERE sc.id IN :classIds")
    List<SchoolClass> findByIds(@Param("classIds") List<Long> classIds);

    @Query("SELECT sc FROM SchoolClass sc JOIN sc.subjects s WHERE s.id = :subjectId")
    List<SchoolClass> findBySubjectId(@Param("subjectId") Long subjectId);

    List<SchoolClass> findByAcademicYear(String academicYear);
}