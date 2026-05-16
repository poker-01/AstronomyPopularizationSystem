package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.ExplorationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExplorationEventRepository extends JpaRepository<ExplorationEvent, Long> {

    List<ExplorationEvent> findAllByOrderByYearAscSortOrderAsc();
}
