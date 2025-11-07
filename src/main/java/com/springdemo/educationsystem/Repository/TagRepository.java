package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    @Query("SELECT t FROM Tag t ORDER BY t.name")
    List<Tag> findAllOrderedByName();

    List<Tag> findByIdIn(List<Long> ids);
}