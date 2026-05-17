package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.AstroEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AstroEventRepository extends JpaRepository<AstroEvent, Long> {

    @Query("SELECT e FROM AstroEvent e WHERE e.startTime < :rangeEnd AND e.endTime >= :rangeStart ORDER BY e.startTime ASC")
    List<AstroEvent> findOverlapping(@Param("rangeStart") LocalDateTime rangeStart,
                                     @Param("rangeEnd") LocalDateTime rangeEnd);

    List<AstroEvent> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);

    boolean existsByTitleAndStartTime(String title, LocalDateTime startTime);
}
