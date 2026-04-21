package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findBySchoolId(Long schoolId);

    List<SchoolClass> findBySchoolIdOrderByActiveDescNameAscAcademicYearAsc(Long schoolId);

    List<SchoolClass> findBySchoolIdAndActiveTrueOrderByNameAscAcademicYearAsc(Long schoolId);

    @Query("""
            select count(sc) > 0
            from SchoolClass sc
            where sc.school.id = :schoolId
              and lower(sc.name) = lower(:name)
              and sc.academicYear = :academicYear
              and (:excludeId is null or sc.id <> :excludeId)
            """)
    boolean existsDuplicate(@Param("schoolId") Long schoolId,
                            @Param("name") String name,
                            @Param("academicYear") String academicYear,
                            @Param("excludeId") Long excludeId);
}