package com.springboot.backendserver.controller.admin;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.service.AdminQuestionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/questions")
public class AdminQuestionController {

    private final AdminQuestionService adminQuestionService;

    public AdminQuestionController(AdminQuestionService adminQuestionService) {
        this.adminQuestionService = adminQuestionService;
    }

    @GetMapping
    public ApiResponse<PageResult<QuestionDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String difficulty) {
        return ApiResponse.ok(adminQuestionService.list(page, size, keyword, difficulty));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionDto> get(@PathVariable Long id) {
        return ApiResponse.ok(adminQuestionService.get(id));
    }

    @PostMapping
    public ApiResponse<QuestionDto> create(@RequestBody QuestionSaveRequest request) {
        return ApiResponse.ok(adminQuestionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionDto> update(@PathVariable Long id, @RequestBody QuestionSaveRequest request) {
        return ApiResponse.ok(adminQuestionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminQuestionService.delete(id);
        return ApiResponse.ok("已删除", null);
    }

    @PostMapping("/import")
    public ApiResponse<Integer> importJson(@RequestBody QuestionImportRequest request) {
        return ApiResponse.ok(adminQuestionService.importBatch(request));
    }
}
