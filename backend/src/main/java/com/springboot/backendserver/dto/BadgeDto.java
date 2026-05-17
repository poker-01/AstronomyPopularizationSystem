package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Badge;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BadgeDto {
    private Long id;
    private String name;
    private String iconUrl;
    private String description;
    private String ruleType;
    private Integer ruleValue;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BadgeDto from(Badge badge) {
        BadgeDto dto = new BadgeDto();
        dto.setId(badge.getId());
        dto.setName(badge.getName());
        dto.setIconUrl(badge.getIconUrl());
        dto.setDescription(badge.getDescription());
        dto.setRuleType(badge.getRuleType().name());
        dto.setRuleValue(badge.getRuleValue());
        dto.setEnabled(badge.getEnabled());
        dto.setCreatedAt(badge.getCreatedAt());
        dto.setUpdatedAt(badge.getUpdatedAt());
        return dto;
    }
}
