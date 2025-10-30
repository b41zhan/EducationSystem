package nba.studix.authservice.Repository;

import nba.studix.authservice.Entity.ActiveToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ActiveTokenRepository extends JpaRepository<ActiveToken, String> {
    Optional<ActiveToken> findByToken(String token);

    @Query("SELECT at FROM ActiveToken at WHERE at.userId = :userId")
    Optional<ActiveToken> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ActiveToken at WHERE at.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    void deleteByToken(String token);

    boolean existsByToken(String token);
}