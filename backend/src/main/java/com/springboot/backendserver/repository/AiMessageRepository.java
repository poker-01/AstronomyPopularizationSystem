package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

    List<AiMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
