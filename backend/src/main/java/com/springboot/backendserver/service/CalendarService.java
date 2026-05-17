package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.context.AuthContext;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.entity.*;
import com.springboot.backendserver.repository.AstroEventRepository;
import com.springboot.backendserver.repository.EventSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final AstroEventRepository astroEventRepository;
    private final EventSubscriptionRepository subscriptionRepository;

    public CalendarService(AstroEventRepository astroEventRepository,
                           EventSubscriptionRepository subscriptionRepository) {
        this.astroEventRepository = astroEventRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<AstroEventDto> listByMonth(int year, int month) {
        validateYearMonth(year, month);
        LocalDateTime rangeStart = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime rangeEnd = rangeStart.plusMonths(1);
        return mapWithSubscription(astroEventRepository.findOverlapping(rangeStart, rangeEnd));
    }

    public AstroEventDto get(Long id) {
        AstroEvent event = findOrThrow(id);
        Boolean subscribed = resolveSubscribed(id);
        return AstroEventDto.from(event, subscribed);
    }

    public CalendarUpcomingDto upcoming() {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime weekEnd = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
                .plusDays(1).atStartOfDay();

        List<AstroEvent> todayEvents = astroEventRepository.findOverlapping(dayStart, dayEnd);
        List<AstroEvent> weekEvents = astroEventRepository.findOverlapping(dayStart, weekEnd);

        CalendarUpcomingDto dto = new CalendarUpcomingDto();
        dto.setToday(mapWithSubscription(todayEvents));
        dto.setThisWeek(mapWithSubscription(weekEvents));
        dto.setSubscribedUpcoming(listMineRemindersInternal());
        return dto;
    }

    public List<EventReminderDto> listMineReminders() {
        User user = AuthContext.require();
        return buildReminders(findUpcomingSubscriptions(user.getId()));
    }

    @Transactional
    public void subscribe(Long eventId, String notifyChannel) {
        User user = AuthContext.require();
        findOrThrow(eventId);
        if (subscriptionRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw BusinessException.badRequest("已订阅该事件");
        }
        EventSubscription sub = new EventSubscription();
        sub.setUserId(user.getId());
        sub.setEventId(eventId);
        sub.setNotifyChannel(parseChannel(notifyChannel));
        subscriptionRepository.save(sub);
    }

    @Transactional
    public void unsubscribe(Long eventId) {
        User user = AuthContext.require();
        if (!subscriptionRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw BusinessException.notFound("未订阅该事件");
        }
        subscriptionRepository.deleteByUserIdAndEventId(user.getId(), eventId);
    }

    private List<EventReminderDto> listMineRemindersInternal() {
        User user = AuthContext.get();
        if (user == null) {
            return List.of();
        }
        return buildReminders(findUpcomingSubscriptions(user.getId()));
    }

    private List<EventSubscription> findUpcomingSubscriptions(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<EventSubscription> subs = subscriptionRepository.findByUserId(userId);
        if (subs.isEmpty()) {
            return List.of();
        }
        Set<Long> eventIds = subs.stream().map(EventSubscription::getEventId).collect(Collectors.toSet());
        Set<Long> upcomingIds = astroEventRepository.findAllById(eventIds).stream()
                .filter(e -> !e.getEndTime().isBefore(now))
                .map(AstroEvent::getId)
                .collect(Collectors.toSet());
        return subs.stream()
                .filter(s -> upcomingIds.contains(s.getEventId()))
                .toList();
    }

    private List<EventReminderDto> buildReminders(List<EventSubscription> subs) {
        if (subs.isEmpty()) {
            return List.of();
        }
        Set<Long> eventIds = subs.stream().map(EventSubscription::getEventId).collect(Collectors.toSet());
        Map<Long, AstroEvent> events = astroEventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(AstroEvent::getId, e -> e));
        LocalDateTime now = LocalDateTime.now();
        List<EventReminderDto> result = new ArrayList<>();
        for (EventSubscription sub : subs) {
            AstroEvent event = events.get(sub.getEventId());
            if (event == null) {
                continue;
            }
            EventReminderDto dto = new EventReminderDto();
            dto.setSubscriptionId(sub.getId());
            dto.setEventId(event.getId());
            dto.setTitle(event.getTitle());
            dto.setEventType(event.getEventType().name());
            dto.setEventTypeLabel(AstroEventDto.labelOf(event.getEventType()));
            dto.setStartTime(event.getStartTime());
            dto.setEndTime(event.getEndTime());
            dto.setDescription(event.getDescription());
            dto.setNotifyChannel(sub.getNotifyChannel().name());
            dto.setReminded(sub.getReminded());
            dto.setMinutesUntilStart(Duration.between(now, event.getStartTime()).toMinutes());
            result.add(dto);
        }
        result.sort(Comparator.comparingLong(EventReminderDto::getMinutesUntilStart));
        return result;
    }

    private List<AstroEventDto> mapWithSubscription(List<AstroEvent> events) {
        Set<Long> subscribedIds = subscribedEventIds();
        return events.stream()
                .map(e -> AstroEventDto.from(e, subscribedIds.contains(e.getId())))
                .toList();
    }

    private Set<Long> subscribedEventIds() {
        User user = AuthContext.get();
        if (user == null) {
            return Set.of();
        }
        return subscriptionRepository.findByUserId(user.getId()).stream()
                .map(EventSubscription::getEventId)
                .collect(Collectors.toSet());
    }

    private Boolean resolveSubscribed(Long eventId) {
        User user = AuthContext.get();
        if (user == null) {
            return false;
        }
        return subscriptionRepository.existsByUserIdAndEventId(user.getId(), eventId);
    }

    private AstroEvent findOrThrow(Long id) {
        return astroEventRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("天文事件不存在"));
    }

    private NotifyChannel parseChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return NotifyChannel.IN_APP;
        }
        try {
            return NotifyChannel.valueOf(channel.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw BusinessException.badRequest("通知渠道须为 IN_APP 或 EMAIL");
        }
    }

    private void validateYearMonth(int year, int month) {
        if (year < 1970 || year > 2100) {
            throw BusinessException.badRequest("年份无效");
        }
        if (month < 1 || month > 12) {
            throw BusinessException.badRequest("月份须在 1–12 之间");
        }
    }
}
