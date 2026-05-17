package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.QuizPublicDto;
import com.springboot.backendserver.dto.QuizSubmitRequest;
import com.springboot.backendserver.dto.QuizSubmitResultDto;
import com.springboot.backendserver.dto.QuizSummaryDto;
import com.springboot.backendserver.service.QuizService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ApiResponse<List<QuizSummaryDto>> list() {
        return ApiResponse.ok(quizService.listQuizzes());
    }

    @GetMapping("/{id}")
    public ApiResponse<QuizPublicDto> get(@PathVariable Long id) {
        return ApiResponse.ok(quizService.getQuizForPlay(id));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<QuizSubmitResultDto> submit(@PathVariable Long id, @RequestBody QuizSubmitRequest request) {
        return ApiResponse.ok(quizService.submit(id, request));
    }
}
