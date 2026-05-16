package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.ExplorationEventDto;
import com.springboot.backendserver.service.ExplorationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exploration")
public class ExplorationController {

    private final ExplorationService explorationService;

    public ExplorationController(ExplorationService explorationService) {
        this.explorationService = explorationService;
    }

    @GetMapping("/events")
    public ApiResponse<List<ExplorationEventDto>> events() {
        return ApiResponse.ok(explorationService.listEvents());
    }
}
