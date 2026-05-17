package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.dto.AstroEventDto;
import com.springboot.backendserver.dto.AstroEventSaveRequest;
import com.springboot.backendserver.dto.CalendarImportResultDto;
import com.springboot.backendserver.entity.AstroEvent;
import com.springboot.backendserver.entity.AstroEventType;
import com.springboot.backendserver.repository.AstroEventRepository;
import com.springboot.backendserver.repository.EventSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminCalendarService {

    private final AstroEventRepository astroEventRepository;
    private final EventSubscriptionRepository subscriptionRepository;
    private final AstronomyImportService astronomyImportService;

    public AdminCalendarService(AstroEventRepository astroEventRepository,
                                  EventSubscriptionRepository subscriptionRepository,
                                  AstronomyImportService astronomyImportService) {
        this.astroEventRepository = astroEventRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.astronomyImportService = astronomyImportService;
    }

    public List<AstroEventDto> list() {
        return astroEventRepository.findAll().stream()
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .map(AstroEventDto::from)
                .toList();
    }

    public AstroEventDto get(Long id) {
        return AstroEventDto.from(findOrThrow(id));
    }

    @Transactional
    public AstroEventDto create(AstroEventSaveRequest request) {
        validate(request);
        AstroEvent event = new AstroEvent();
        apply(event, request);
        astroEventRepository.save(event);
        return AstroEventDto.from(event);
    }

    @Transactional
    public AstroEventDto update(Long id, AstroEventSaveRequest request) {
        AstroEvent event = findOrThrow(id);
        validate(request);
        apply(event, request);
        astroEventRepository.save(event);
        return AstroEventDto.from(event);
    }

    @Transactional
    public void delete(Long id) {
        if (!astroEventRepository.existsById(id)) {
            throw BusinessException.notFound("天文事件不存在");
        }
        subscriptionRepository.deleteByEventId(id);
        astroEventRepository.deleteById(id);
    }

    @Transactional
    public CalendarImportResultDto importPublicData(Integer year) {
        int y = year != null ? year : LocalDateTime.now().getYear();
        if (y < 1970 || y > 2100) {
            throw BusinessException.badRequest("年份无效");
        }
        AstronomyImportService.ImportStats stats = astronomyImportService.importForYear(y);
        CalendarImportResultDto dto = new CalendarImportResultDto();
        dto.setImported(stats.imported);
        dto.setSkipped(stats.skipped);
        dto.setSource(stats.source);
        dto.setMessage(String.format("已导入 %d 条，跳过重复 %d 条", stats.imported, stats.skipped));
        return dto;
    }

    private AstroEvent findOrThrow(Long id) {
        return astroEventRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("天文事件不存在"));
    }

    private void validate(AstroEventSaveRequest request) {
        if (request == null || !StringUtils.hasText(request.getTitle())) {
            throw BusinessException.badRequest("标题不能为空");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw BusinessException.badRequest("开始与结束时间不能为空");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw BusinessException.badRequest("结束时间不能早于开始时间");
        }
        try {
            AstroEventType.valueOf(request.getEventType());
        } catch (Exception e) {
            throw BusinessException.badRequest("事件类型无效");
        }
    }

    private void apply(AstroEvent event, AstroEventSaveRequest request) {
        event.setTitle(request.getTitle().trim());
        event.setEventType(AstroEventType.valueOf(request.getEventType()));
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setDescription(request.getDescription());
        if (request.getReminderOffsetMinutes() != null && request.getReminderOffsetMinutes() >= 0) {
            event.setReminderOffsetMinutes(request.getReminderOffsetMinutes());
        }
    }
}
