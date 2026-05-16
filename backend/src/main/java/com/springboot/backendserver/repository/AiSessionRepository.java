package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.AiSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiSessionRepository extends JpaRepository<AiSession, Long> {

    List<AiSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<AiSession> findByIdAndUserId(Long id, Long userId);
}
