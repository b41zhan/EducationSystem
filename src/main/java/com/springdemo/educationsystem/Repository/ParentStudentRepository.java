package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    @Query("select ps from ParentStudent ps where ps.parent.user.id = :parentUserId")
    List<ParentStudent> findByParentUserId(Long parentUserId);

    @Query("select count(ps) > 0 from ParentStudent ps where ps.parent.user.id = :parentUserId and ps.student.id = :studentId")
    boolean existsByParentUserIdAndStudentId(Long parentUserId, Long studentId);

    boolean existsByParentIdAndStudentId(Long parentId, Long studentId);

}
