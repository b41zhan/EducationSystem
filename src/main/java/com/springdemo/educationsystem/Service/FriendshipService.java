package com.springdemo.educationsystem.Service;

import com.springdemo.educationsystem.DTO.FriendshipDTO;
import com.springdemo.educationsystem.DTO.UserSearchDTO;
import com.springdemo.educationsystem.Entity.Friendship;
import com.springdemo.educationsystem.Entity.FriendshipStatus;
import com.springdemo.educationsystem.Entity.Notification;
import com.springdemo.educationsystem.Entity.User;
import com.springdemo.educationsystem.Repository.FriendshipRepository;
import com.springdemo.educationsystem.Repository.NotificationRepository;
import com.springdemo.educationsystem.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private static final Logger logger = LoggerFactory.getLogger(FriendshipService.class);

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    public FriendshipService(FriendshipRepository friendshipRepository,
                             UserRepository userRepository,
                             NotificationRepository notificationRepository,
                             AuthService authService) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.authService = authService;
    }

    public FriendshipDTO sendFriendRequest(Long requesterId, Long addresseeId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + requesterId));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + addresseeId));

        if (requesterId.equals(addresseeId)) {
            throw new RuntimeException("Cannot send friend request to yourself");
        }

        // Проверяем, существует ли уже запрос
        Optional<Friendship> existingFriendship = friendshipRepository
                .findFriendshipBetweenUsers(requester, addressee);

        if (existingFriendship.isPresent()) {
            Friendship friendship = existingFriendship.get();
            if (friendship.getStatus() == FriendshipStatus.PENDING) {
                throw new RuntimeException("Friend request already sent");
            } else if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new RuntimeException("You are already friends");
            } else {
                // Если запрос был отклонен, обновляем статус
                friendship.setStatus(FriendshipStatus.PENDING);
                Friendship saved = friendshipRepository.save(friendship);
                createFriendRequestNotification(addressee, requester);
                return convertToDTO(saved);
            }
        }

        // Создаем новый запрос
        Friendship friendship = new Friendship(requester, addressee, FriendshipStatus.PENDING);
        Friendship saved = friendshipRepository.save(friendship);

        // Создаем уведомление для получателя
        createFriendRequestNotification(addressee, requester);

        logger.info("Friend request sent from {} to {}", requesterId, addresseeId);
        return convertToDTO(saved);
    }

    public FriendshipDTO acceptFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("You can only accept requests sent to you");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Friend request is not pending");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship saved = friendshipRepository.save(friendship);

        // Создаем уведомление для отправителя
        createFriendRequestAcceptedNotification(friendship.getRequester(), friendship.getAddressee());

        logger.info("Friend request accepted: {}", friendshipId);
        return convertToDTO(saved);
    }

    public FriendshipDTO rejectFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("You can only reject requests sent to you");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        Friendship saved = friendshipRepository.save(friendship);

        // Создаем уведомление для отправителя
        createFriendRequestRejectedNotification(friendship.getRequester(), friendship.getAddressee());

        logger.info("Friend request rejected: {}", friendshipId);
        return convertToDTO(saved);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        Optional<Friendship> friendship = friendshipRepository
                .findFriendshipBetweenUsers(user, friend);

        if (friendship.isPresent() && friendship.get().getStatus() == FriendshipStatus.ACCEPTED) {
            friendshipRepository.delete(friendship.get());
            logger.info("Friendship removed between {} and {}", userId, friendId);
        } else {
            throw new RuntimeException("Friendship not found");
        }
    }

    public List<FriendshipDTO> getPendingRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return friendshipRepository.findPendingRequestsByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserSearchDTO> getFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return friendshipRepository.findFriendshipsByUser(user)
                .stream()
                .map(friendship -> {
                    User friend = friendship.getRequester().getId().equals(userId)
                            ? friendship.getAddressee()
                            : friendship.getRequester();
                    return convertToUserSearchDTO(friend, "ACCEPTED");
                })
                .collect(Collectors.toList());
    }

    public List<UserSearchDTO> searchUsers(String query, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String searchQuery = "%" + query.toLowerCase() + "%";
        List<User> users = userRepository.findBySearchQuery(searchQuery, currentUserId);

        return users.stream()
                .map(user -> {
                    String friendshipStatus = getFriendshipStatus(currentUser, user);
                    return convertToUserSearchDTO(user, friendshipStatus);
                })
                .collect(Collectors.toList());
    }

    public FriendshipStatsDTO getFriendshipStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long friendsCount = friendshipRepository.findFriendshipsByUser(user).size();
        long pendingCount = friendshipRepository.findPendingRequestsByUser(user).size();

        return new FriendshipStatsDTO(friendsCount, pendingCount);
    }

    private String getFriendshipStatus(User currentUser, User otherUser) {
        if (currentUser.getId().equals(otherUser.getId())) {
            return "SELF";
        }

        Optional<Friendship> friendship = friendshipRepository
                .findFriendshipBetweenUsers(currentUser, otherUser);

        if (friendship.isPresent()) {
            return friendship.get().getStatus().name();
        }

        return "NONE";
    }

    private FriendshipDTO convertToDTO(Friendship friendship) {
        FriendshipDTO dto = new FriendshipDTO();
        dto.setId(friendship.getId());
        dto.setRequesterId(friendship.getRequester().getId());
        dto.setRequesterName(friendship.getRequester().getFirstName() + " " + friendship.getRequester().getLastName());
        dto.setRequesterEmail(friendship.getRequester().getEmail());
        dto.setAddresseeId(friendship.getAddressee().getId());
        dto.setAddresseeName(friendship.getAddressee().getFirstName() + " " + friendship.getAddressee().getLastName());
        dto.setAddresseeEmail(friendship.getAddressee().getEmail());
        dto.setStatus(friendship.getStatus().name());
        dto.setCreatedAt(friendship.getCreatedAt());
        dto.setUpdatedAt(friendship.getUpdatedAt());
        return dto;
    }

    private UserSearchDTO convertToUserSearchDTO(User user, String friendshipStatus) {
        UserSearchDTO dto = new UserSearchDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setProfilePhotoPath(user.getProfilePhotoPath());
        dto.setFriendshipStatus(friendshipStatus);
        return dto;
    }

    private void createFriendRequestNotification(User addressee, User requester) {
        String message = String.format("%s %s отправил(а) вам запрос на дружбу",
                requester.getFirstName(), requester.getLastName());
        Notification notification = new Notification(addressee, message, "friend_request", requester.getId());
        notificationRepository.save(notification);
    }

    private void createFriendRequestAcceptedNotification(User requester, User addressee) {
        String message = String.format("%s %s принял(а) ваш запрос на дружбу",
                addressee.getFirstName(), addressee.getLastName());
        Notification notification = new Notification(requester, message, "friend_request_accepted", addressee.getId());
        notificationRepository.save(notification);
    }

    private void createFriendRequestRejectedNotification(User requester, User addressee) {
        String message = String.format("%s %s отклонил(а) ваш запрос на дружбу",
                addressee.getFirstName(), addressee.getLastName());
        Notification notification = new Notification(requester, message, "friend_request_rejected", addressee.getId());
        notificationRepository.save(notification);
    }

    // DTO для статистики
    public static class FriendshipStatsDTO {
        private long friendsCount;
        private long pendingRequestsCount;

        public FriendshipStatsDTO(long friendsCount, long pendingRequestsCount) {
            this.friendsCount = friendsCount;
            this.pendingRequestsCount = pendingRequestsCount;
        }

        // Геттеры
        public long getFriendsCount() { return friendsCount; }
        public long getPendingRequestsCount() { return pendingRequestsCount; }
    }
}
