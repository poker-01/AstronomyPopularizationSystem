package com.springboot.backendserver.controller.admin;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.QuizDetailDto;
import com.springboot.backendserver.dto.QuizSaveRequest;
import com.springboot.backendserver.dto.QuizSummaryDto;
import com.springboot.backendserver.service.AdminQuizService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/quizzes")
public class AdminQuizController {

    private final AdminQuizService adminQuizService;

    public AdminQuizController(AdminQuizService adminQuizService) {
        this.adminQuizService = adminQuizService;
    }

    @GetMapping
    public ApiResponse<List<QuizSummaryDto>> list() {
        return ApiResponse.ok(adminQuizService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<QuizDetailDto> get(@PathVariable Long id) {
        return ApiResponse.ok(adminQuizService.get(id));
    }

    @PostMapping
    public ApiResponse<QuizDetailDto> create(@RequestBody QuizSaveRequest request) {
        return ApiResponse.ok(adminQuizService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<QuizDetailDto> update(@PathVariable Long id, @RequestBody QuizSaveRequest request) {
        return ApiResponse.ok(adminQuizService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminQuizService.delete(id);
        return ApiResponse.ok("已删除", null);
    }
}
