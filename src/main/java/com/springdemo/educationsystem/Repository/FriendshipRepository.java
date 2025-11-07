package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Friendship;
import com.springdemo.educationsystem.Entity.FriendshipStatus;
import com.springdemo.educationsystem.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Найти запрос дружбы между двумя пользователями
    Optional<Friendship> findByRequesterAndAddressee(User requester, User addressee);

    // Найти все запросы, отправленные пользователем
    List<Friendship> findByRequesterAndStatus(User requester, FriendshipStatus status);

    // Найти все запросы, полученные пользователем
    List<Friendship> findByAddresseeAndStatus(User addressee, FriendshipStatus status);

    // Найти всех друзей пользователя (принятые запросы где пользователь либо отправитель, либо получатель)
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findFriendshipsByUser(@Param("user") User user);

    // Найти все входящие запросы дружбы
    @Query("SELECT f FROM Friendship f WHERE f.addressee = :user AND f.status = 'PENDING'")
    List<Friendship> findPendingRequestsByUser(@Param("user") User user);

    // Проверить, являются ли два пользователя друзьями
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
            "((f.requester = :user1 AND f.addressee = :user2) OR " +
            "(f.requester = :user2 AND f.addressee = :user1)) AND " +
            "f.status = 'ACCEPTED'")
    boolean areFriends(@Param("user1") User user1, @Param("user2") User user2);

    // Найти конкретный запрос дружбы
    @Query("SELECT f FROM Friendship f WHERE " +
            "((f.requester = :user1 AND f.addressee = :user2) OR " +
            "(f.requester = :user2 AND f.addressee = :user1))")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
}