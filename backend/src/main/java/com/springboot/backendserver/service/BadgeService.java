package com.springboot.backendserver.service;

import com.springboot.backendserver.dto.UserBadgeDto;
import com.springboot.backendserver.entity.Badge;
import com.springboot.backendserver.entity.BadgeRuleType;
import com.springboot.backendserver.entity.UserBadge;
import com.springboot.backendserver.entity.UserQuizAttempt;
import com.springboot.backendserver.repository.BadgeRepository;
import com.springboot.backendserver.repository.UserBadgeRepository;
import com.springboot.backendserver.repository.UserQuizAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserQuizAttemptRepository attemptRepository;

    public BadgeService(BadgeRepository badgeRepository,
                        UserBadgeRepository userBadgeRepository,
                        UserQuizAttemptRepository attemptRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.attemptRepository = attemptRepository;
    }

    public List<UserBadgeDto> listMine(Long userId) {
        List<UserBadge> earned = userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId);
        if (earned.isEmpty()) {
            return List.of();
        }
        Map<Long, Badge> badgeMap = badgeRepository.findAllById(
                earned.stream().map(UserBadge::getBadgeId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Badge::getId, b -> b));
        return earned.stream()
                .map(ub -> UserBadgeDto.from(ub, badgeMap.get(ub.getBadgeId())))
                .filter(dto -> dto.getName() != null)
                .toList();
    }

    @Transactional
    public List<UserBadgeDto> evaluateAndAward(Long userId, int latestScore) {
        List<Badge> rules = badgeRepository.findByEnabledTrueOrderByIdAsc();
        if (rules.isEmpty()) {
            return List.of();
        }

        long attemptCount = attemptRepository.countByUserId(userId);
        Integer maxScore = attemptRepository.findMaxScoreByUserId(userId);
        int bestScore = maxScore == null ? latestScore : Math.max(maxScore, latestScore);
        int streak = computeStreak(userId);

        List<UserBadgeDto> newlyEarned = new ArrayList<>();
        for (Badge badge : rules) {
            if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) {
                continue;
            }
            if (matchesRule(badge, latestScore, attemptCount, bestScore, streak)) {
                UserBadge ub = new UserBadge();
                ub.setUserId(userId);
                ub.setBadgeId(badge.getId());
                ub.setEarnedAt(LocalDateTime.now());
                userBadgeRepository.save(ub);
                newlyEarned.add(UserBadgeDto.from(ub, badge));
            }
        }
        return newlyEarned;
    }

    private boolean matchesRule(Badge badge, int latestScore, long attemptCount, int bestScore, int streak) {
        int threshold = badge.getRuleValue() == null ? 0 : badge.getRuleValue();
        return switch (badge.getRuleType()) {
            case QUIZ_SCORE -> latestScore >= threshold || bestScore >= threshold;
            case QUIZ_COUNT -> attemptCount >= threshold;
            case STREAK -> streak >= threshold;
        };
    }

    private int computeStreak(Long userId) {
        List<UserQuizAttempt> attempts = attemptRepository.findByUserIdOrderByFinishedAtDesc(userId);
        if (attempts.isEmpty()) {
            return 0;
        }
        Set<LocalDate> dates = attempts.stream()
                .map(a -> a.getFinishedAt().toLocalDate())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LocalDate cursor = LocalDate.now();
        if (!dates.contains(cursor) && !dates.contains(cursor.minusDays(1))) {
            return 0;
        }
        if (!dates.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }

        int streak = 0;
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
