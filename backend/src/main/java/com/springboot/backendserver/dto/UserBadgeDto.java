package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Badge;
import com.springboot.backendserver.entity.UserBadge;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBadgeDto {
    private Long badgeId;
    private String name;
    private String iconUrl;
    private String description;
    private String ruleType;
    private Integer ruleValue;
    private LocalDateTime earnedAt;

    public static UserBadgeDto from(UserBadge ub, Badge badge) {
        UserBadgeDto dto = new UserBadgeDto();
        dto.setBadgeId(badge.getId());
        dto.setName(badge.getName());
        dto.setIconUrl(badge.getIconUrl());
        dto.setDescription(badge.getDescription());
        dto.setRuleType(badge.getRuleType().name());
        dto.setRuleValue(badge.getRuleValue());
        dto.setEarnedAt(ub.getEarnedAt());
        return dto;
    }
}
