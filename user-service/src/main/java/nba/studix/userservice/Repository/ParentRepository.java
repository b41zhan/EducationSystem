package nba.studix.userservice.Repository;

import nba.studix.userservice.Entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findById(Long userId);
    boolean existsById(Long userId);
}