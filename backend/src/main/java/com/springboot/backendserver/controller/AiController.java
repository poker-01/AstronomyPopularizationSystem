package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.AiChatRequest;
import com.springboot.backendserver.dto.AiChatResponse;
import com.springboot.backendserver.dto.AiSessionDto;
import com.springboot.backendserver.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        return ApiResponse.ok(aiService.chat(request));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AiChatRequest request) {
        return aiService.chatStream(request);
    }

    @GetMapping("/sessions")
    public ApiResponse<List<AiSessionDto>> sessions() {
        return ApiResponse.ok(aiService.listSessions());
    }

    @PostMapping("/sessions")
    public ApiResponse<AiSessionDto> createSession() {
        return ApiResponse.ok(aiService.newSession());
    }

    @GetMapping("/sessions/{id}")
    public ApiResponse<AiSessionDto> session(@PathVariable Long id) {
        return ApiResponse.ok(aiService.getSession(id));
    }

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> deleteSession(@PathVariable Long id) {
        aiService.deleteSession(id);
        return ApiResponse.ok("已删除", null);
    }
}
