package com.springboot.backendserver.service;

import com.springboot.backendserver.entity.AstroEvent;
import com.springboot.backendserver.entity.AstroEventType;
import com.springboot.backendserver.repository.AstroEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AstronomyImportService {

    private final AstroEventRepository astroEventRepository;
    private final RestClient jplClient;

    public AstronomyImportService(AstroEventRepository astroEventRepository, RestClient.Builder restClientBuilder) {
        this.astroEventRepository = astroEventRepository;
        this.jplClient = restClientBuilder.baseUrl("https://ssd-api.jpl.nasa.gov").build();
    }

    public ImportStats importForYear(int year) {
        ImportStats stats = new ImportStats();
        stats.source = "NASA JPL CAD + annual meteor showers";

        importMeteorShowers(year, stats);
        importJplCloseApproaches(year, stats);

        return stats;
    }

    private void importMeteorShowers(int year, ImportStats stats) {
        List<MeteorTemplate> templates = List.of(
                new MeteorTemplate("象限仪座流星雨极大", 1, 4, "北半球冬季流星雨，峰值约 1 月 3–4 日。"),
                new MeteorTemplate("英仙座流星雨极大", 8, 13, "夏季经典流星雨，峰值约 8 月 12–13 日。"),
                new MeteorTemplate("猎户座流星雨极大", 10, 22, "由哈雷彗星尘埃引起，峰值约 10 月 21–22 日。"),
                new MeteorTemplate("狮子座流星雨极大", 11, 18, "著名流星雨，峰值约 11 月 17–18 日。"),
                new MeteorTemplate("双子座流星雨极大", 12, 14, "全年最稳定流星雨之一，峰值约 12 月 13–14 日。"),
                new MeteorTemplate("小熊座流星雨极大", 12, 23, "冬季小流星雨，峰值约 12 月 22–23 日。")
        );
        for (MeteorTemplate t : templates) {
            saveIfNew(
                    t.title,
                    AstroEventType.METEOR_SHOWER,
                    LocalDate.of(year, t.month, t.day).atTime(2, 0),
                    LocalDate.of(year, t.month, t.day).atTime(23, 59),
                    t.description,
                    "IMO annual calendar",
                    stats
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void importJplCloseApproaches(int year, ImportStats stats) {
        String dateMin = year + "-01-01";
        String dateMax = year + "-12-31";
        try {
            Map<String, Object> body = jplClient.get()
                    .uri(uri -> uri.path("/cad.api")
                            .queryParam("date-min", dateMin)
                            .queryParam("date-max", dateMax)
                            .queryParam("dist-max", "0.05")
                            .queryParam("sort", "date")
                            .build())
                    .retrieve()
                    .body(Map.class);
            if (body == null || !body.containsKey("data")) {
                return;
            }
            List<String> fields = (List<String>) body.get("fields");
            List<List<Object>> rows = (List<List<Object>>) body.get("data");
            if (fields == null || rows == null) {
                return;
            }
            int dateIdx = fields.indexOf("cd");
            int nameIdx = fields.indexOf("fullname");
            int distIdx = fields.indexOf("dist");
            int velIdx = fields.indexOf("v_rel");
            int limit = Math.min(rows.size(), 30);
            for (int i = 0; i < limit; i++) {
                List<Object> row = rows.get(i);
                String date = stringAt(row, dateIdx);
                String name = stringAt(row, nameIdx);
                String dist = stringAt(row, distIdx);
                String vel = stringAt(row, velIdx);
                if (date == null || name == null) {
                    continue;
                }
                LocalDateTime start = parseJplDate(date);
                if (start == null) {
                    continue;
                }
                String title = "近地天体接近：" + name.trim();
                String desc = String.format(
                        "据 NASA JPL 近地天体数据库，该天体将于 %s 附近接近地球，最近距离约 %s AU，相对速度约 %s km/s。",
                        date, dist != null ? dist : "—", vel != null ? vel : "—"
                );
                saveIfNew(title, AstroEventType.OTHER, start, start.plusHours(6), desc, "NASA JPL CAD API", stats);
            }
        } catch (Exception ignored) {
            // JPL API unavailable; meteor showers still imported
        }
    }

    private void saveIfNew(String title, AstroEventType type, LocalDateTime start, LocalDateTime end,
                           String description, String source, ImportStats stats) {
        if (astroEventRepository.existsByTitleAndStartTime(title, start)) {
            stats.skipped++;
            return;
        }
        AstroEvent event = new AstroEvent();
        event.setTitle(title);
        event.setEventType(type);
        event.setStartTime(start);
        event.setEndTime(end);
        event.setDescription(description);
        event.setReminderOffsetMinutes(1440);
        event.setSource(source);
        astroEventRepository.save(event);
        stats.imported++;
    }

    private static String stringAt(List<Object> row, int index) {
        if (index < 0 || index >= row.size() || row.get(index) == null) {
            return null;
        }
        return row.get(index).toString();
    }

    private static LocalDateTime parseJplDate(String cd) {
        try {
            if (cd.length() >= 10) {
                LocalDate d = LocalDate.parse(cd.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
                return d.atTime(LocalTime.of(12, 0));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private record MeteorTemplate(String title, int month, int day, String description) {}

    public static class ImportStats {
        public int imported;
        public int skipped;
        public String source;
    }
}
