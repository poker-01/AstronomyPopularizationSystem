package com.springboot.backendserver.service;

import com.springboot.backendserver.dto.ExplorationEventDto;
import com.springboot.backendserver.repository.ExplorationEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExplorationService {

    private final ExplorationEventRepository explorationEventRepository;

    public ExplorationService(ExplorationEventRepository explorationEventRepository) {
        this.explorationEventRepository = explorationEventRepository;
    }

    public List<ExplorationEventDto> listEvents() {
        return explorationEventRepository.findAllByOrderByYearAscSortOrderAsc().stream()
                .map(ExplorationEventDto::from)
                .toList();
    }
}
