package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.AiSession;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiSessionDto {

    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AiMessageDto> messages;

    public static AiSessionDto from(AiSession session) {
        AiSessionDto dto = new AiSessionDto();
        dto.setId(session.getId());
        dto.setTitle(session.getTitle());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        return dto;
    }
}
