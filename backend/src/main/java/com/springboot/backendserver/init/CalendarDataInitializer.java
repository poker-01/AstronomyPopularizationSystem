package com.springboot.backendserver.init;

import com.springboot.backendserver.entity.AstroEvent;
import com.springboot.backendserver.entity.AstroEventType;
import com.springboot.backendserver.repository.AstroEventRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Order(3)
public class CalendarDataInitializer {

    private final AstroEventRepository astroEventRepository;

    public CalendarDataInitializer(AstroEventRepository astroEventRepository) {
        this.astroEventRepository = astroEventRepository;
    }

    @PostConstruct
    public void seed() {
        if (astroEventRepository.count() > 0) {
            return;
        }
        int year = LocalDate.now().getYear();
        seedEvent("五月满月（花月）", AstroEventType.OTHER,
                LocalDate.of(year, 5, 12).atTime(18, 0),
                LocalDate.of(year, 5, 12).atTime(23, 59),
                "五月满月又称花月，是春季观测月球地貌的良机。", 60);
        seedEvent("木星与金星近距相合", AstroEventType.PLANETARY_CONJUNCTION,
                LocalDate.of(year, 5, 18).atTime(20, 0),
                LocalDate.of(year, 5, 19).atTime(4, 0),
                "日落后西方低空，木星与金星视距较近，适合肉眼与双筒望远镜观测。", 120);
        seedEvent("宝瓶座η流星雨活跃期", AstroEventType.METEOR_SHOWER,
                LocalDate.of(year, 5, 5).atTime(1, 0),
                LocalDate.of(year, 5, 7).atTime(5, 0),
                "南半球观测条件更佳的流星雨，北半球可在黎明前低空尝试。", 1440);
        seedEvent("月偏食（示例）", AstroEventType.LUNAR_ECLIPSE,
                LocalDate.of(year, 9, 18).atTime(2, 0),
                LocalDate.of(year, 9, 18).atTime(6, 30),
                "示例月食事件，具体时刻请以当年天文年历为准。", 1440);
    }

    private void seedEvent(String title, AstroEventType type, LocalDateTime start, LocalDateTime end,
                           String description, int reminderMinutes) {
        AstroEvent event = new AstroEvent();
        event.setTitle(title);
        event.setEventType(type);
        event.setStartTime(start);
        event.setEndTime(end);
        event.setDescription(description);
        event.setReminderOffsetMinutes(reminderMinutes);
        event.setSource("seed");
        astroEventRepository.save(event);
    }
}
