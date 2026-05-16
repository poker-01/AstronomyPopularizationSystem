package com.springboot.backendserver.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiChatResponse {

    private Long sessionId;
    private String reply;
    private List<AiMessageDto> messages;
}
