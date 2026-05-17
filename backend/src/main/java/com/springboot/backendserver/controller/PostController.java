package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.service.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ApiResponse<PageResult<PostSummaryDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean following,
            @RequestParam(required = false) Long userId) {
        return ApiResponse.ok(postService.listPosts(page, size, following, userId));
    }

    @GetMapping("/mine/pending")
    public ApiResponse<PageResult<PostSummaryDto>> myPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(postService.listMyPending(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailDto> get(@PathVariable Long id) {
        return ApiResponse.ok(postService.getPost(id));
    }

    @PostMapping
    public ApiResponse<PostDetailDto> create(@RequestBody PostCreateRequest request) {
        return ApiResponse.ok(postService.createPost(request));
    }

    @GetMapping("/{id}/comments")
    public ApiResponse<List<CommentDto>> listComments(@PathVariable Long id) {
        return ApiResponse.ok(postService.listComments(id));
    }

    @PostMapping("/{id}/comments")
    public ApiResponse<CommentDto> createComment(@PathVariable Long id, @RequestBody CommentCreateRequest request) {
        return ApiResponse.ok(postService.createComment(id, request));
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Void> like(@PathVariable Long id) {
        postService.likePost(id);
        return ApiResponse.ok("已点赞", null);
    }

    @DeleteMapping("/{id}/like")
    public ApiResponse<Void> unlike(@PathVariable Long id) {
        postService.unlikePost(id);
        return ApiResponse.ok("已取消点赞", null);
    }
}
