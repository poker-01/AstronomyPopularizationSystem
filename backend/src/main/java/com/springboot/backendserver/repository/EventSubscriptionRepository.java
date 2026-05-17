package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.EventSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventSubscriptionRepository extends JpaRepository<EventSubscription, Long> {

    Optional<EventSubscription> findByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    void deleteByUserIdAndEventId(Long userId, Long eventId);

    List<EventSubscription> findByUserId(Long userId);

    void deleteByEventId(Long eventId);
}
