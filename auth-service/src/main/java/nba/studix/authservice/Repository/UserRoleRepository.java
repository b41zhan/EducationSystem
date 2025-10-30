package nba.studix.authservice.Repository;

import nba.studix.authservice.Entity.User;
import nba.studix.authservice.Entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(User user);

    @Query("SELECT ur.roleName FROM UserRole ur WHERE ur.user.id = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);

    Optional<UserRole> findByUserAndRoleName(User user, String roleName);

    void deleteByUser(User user);
}
