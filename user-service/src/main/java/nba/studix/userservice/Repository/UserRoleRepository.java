package nba.studix.userservice.Repository;

import nba.studix.userservice.Entity.Role;
import nba.studix.userservice.Entity.User;
import nba.studix.userservice.Entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(User user);

    @Query("SELECT ur.role FROM UserRole ur WHERE ur.user.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.role = :role")
    List<UserRole> findByRole(@Param("role") Role role);

    Optional<UserRole> findByUserAndRole(User user, Role role);

    void deleteByUser(User user);

    boolean existsByUserAndRole(User user, Role role);
}