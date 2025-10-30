package nba.studix.userservice.Repository;

import nba.studix.userservice.Entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    List<School> findByNameContainingIgnoreCase(String name);
}
