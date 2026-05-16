package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.AiMessage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiMessageDto {

    private Long id;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    public static AiMessageDto from(AiMessage message) {
        AiMessageDto dto = new AiMessageDto();
        dto.setId(message.getId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
