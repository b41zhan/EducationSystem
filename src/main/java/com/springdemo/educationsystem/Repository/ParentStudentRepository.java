package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {

    @Query("select ps from ParentStudent ps where ps.parent.user.id = :parentUserId")
    List<ParentStudent> findByParentUserId(Long parentUserId);

    @Query("select count(ps) > 0 from ParentStudent ps where ps.parent.user.id = :parentUserId and ps.student.id = :studentId")
    boolean existsByParentUserIdAndStudentId(Long parentUserId, Long studentId);

    boolean existsByParentIdAndStudentId(Long parentId, Long studentId);

    List<ParentStudent> findByParentId(Long parentId);

    List<ParentStudent> findByStudentId(Long studentId);

    Optional<ParentStudent> findByParentIdAndStudentId(Long parentId, Long studentId);

    void deleteByParentIdAndStudentId(Long parentId, Long studentId);
}