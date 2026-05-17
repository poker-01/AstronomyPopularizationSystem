package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.dto.BadgeDto;
import com.springboot.backendserver.dto.BadgeSaveRequest;
import com.springboot.backendserver.entity.Badge;
import com.springboot.backendserver.entity.BadgeRuleType;
import com.springboot.backendserver.repository.BadgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminBadgeService {

    private final BadgeRepository badgeRepository;

    public AdminBadgeService(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    public List<BadgeDto> list() {
        return badgeRepository.findAll().stream().map(BadgeDto::from).toList();
    }

    public BadgeDto get(Long id) {
        return BadgeDto.from(findOrThrow(id));
    }

    @Transactional
    public BadgeDto create(BadgeSaveRequest request) {
        validate(request);
        Badge badge = new Badge();
        apply(badge, request);
        badgeRepository.save(badge);
        return BadgeDto.from(badge);
    }

    @Transactional
    public BadgeDto update(Long id, BadgeSaveRequest request) {
        Badge badge = findOrThrow(id);
        validate(request);
        apply(badge, request);
        badgeRepository.save(badge);
        return BadgeDto.from(badge);
    }

    @Transactional
    public void delete(Long id) {
        if (!badgeRepository.existsById(id)) {
            throw BusinessException.notFound("徽章不存在");
        }
        badgeRepository.deleteById(id);
    }

    private Badge findOrThrow(Long id) {
        return badgeRepository.findById(id).orElseThrow(() -> BusinessException.notFound("徽章不存在"));
    }

    private void validate(BadgeSaveRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw BusinessException.badRequest("徽章名称不能为空");
        }
        try {
            BadgeRuleType.valueOf(request.getRuleType());
        } catch (Exception e) {
            throw BusinessException.badRequest("规则类型须为 QUIZ_SCORE、QUIZ_COUNT 或 STREAK");
        }
        if (request.getRuleValue() == null || request.getRuleValue() < 0) {
            throw BusinessException.badRequest("规则阈值须为非负整数");
        }
    }

    private void apply(Badge badge, BadgeSaveRequest request) {
        badge.setName(request.getName().trim());
        badge.setIconUrl(request.getIconUrl());
        badge.setDescription(request.getDescription());
        badge.setRuleType(BadgeRuleType.valueOf(request.getRuleType()));
        badge.setRuleValue(request.getRuleValue());
        if (request.getEnabled() != null) {
            badge.setEnabled(request.getEnabled());
        }
    }
}
