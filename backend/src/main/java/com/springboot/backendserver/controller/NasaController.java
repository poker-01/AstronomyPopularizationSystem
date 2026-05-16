package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.PlanetDto;
import com.springboot.backendserver.service.NasaApiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nasa")
public class NasaController {

    private final NasaApiService nasaApiService;

    public NasaController(NasaApiService nasaApiService) {
        this.nasaApiService = nasaApiService;
    }

    @GetMapping("/apod")
    public ApiResponse<Map<String, Object>> apod() {
        return ApiResponse.ok(nasaApiService.getApod());
    }

    @GetMapping("/planets")
    public ApiResponse<List<PlanetDto>> planets() {
        return ApiResponse.ok(nasaApiService.getPlanets());
    }

    @GetMapping("/planets/{id}")
    public ApiResponse<PlanetDto> planet(@PathVariable String id) {
        PlanetDto planet = nasaApiService.getPlanet(id);
        if (planet == null) {
            return ApiResponse.fail(404, "行星不存在");
        }
        return ApiResponse.ok(planet);
    }
}
