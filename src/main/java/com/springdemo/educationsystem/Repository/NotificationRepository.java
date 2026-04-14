package com.springdemo.educationsystem.Repository;

import com.springdemo.educationsystem.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Заменяем старые методы на новые, которые фильтруют скрытые уведомления
    List<Notification> findByUserIdAndHiddenFalseOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadFalseAndHiddenFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalseAndHiddenFalse(Long userId);
}
