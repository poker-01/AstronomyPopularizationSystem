package com.springboot.backendserver.service;

import com.springboot.backendserver.dto.PlanetDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NasaApiService {

    private final RestClient nasaClient;
    private final RestClient imagesClient;
    private final String apiKey;

    private final Map<String, CachedImage> imageCache = new ConcurrentHashMap<>();
    private final Map<String, CachedApod> apodCache = new ConcurrentHashMap<>();

    private static final long CACHE_TTL_MS = 60 * 60 * 1000L;

    public NasaApiService(RestClient.Builder restClientBuilder,
                          @Value("${nasa.api.key:DEMO_KEY}") String apiKey) {
        this.apiKey = apiKey;
        this.nasaClient = restClientBuilder.baseUrl("https://api.nasa.gov").build();
        this.imagesClient = restClientBuilder.baseUrl("https://images-api.nasa.gov").build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getApod() {
        CachedApod cached = apodCache.get("today");
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }
        try {
            Map<String, Object> data = nasaClient.get()
                    .uri(uri -> uri.path("/planetary/apod")
                            .queryParam("api_key", apiKey)
                            .build())
                    .retrieve()
                    .body(Map.class);
            if (data != null) {
                apodCache.put("today", new CachedApod(data, Instant.now().toEpochMilli()));
                return data;
            }
        } catch (Exception ignored) {
            // fallback below
        }
        return fallbackApod();
    }

    public List<PlanetDto> getPlanets() {
        List<PlanetDto> planets = new ArrayList<>(basePlanets());
        planets.parallelStream().forEach(planet -> {
            String imageUrl = fetchPlanetImage(planet.getNameEn());
            if (imageUrl != null) {
                planet.setImageUrl(imageUrl);
                planet.setImageCredit("NASA Image and Video Library");
            }
        });
        return planets;
    }

    public PlanetDto getPlanet(String id) {
        return getPlanets().stream()
                .filter(p -> p.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    private String fetchPlanetImage(String englishName) {
        CachedImage cached = imageCache.get(englishName);
        if (cached != null && !cached.isExpired()) {
            return cached.url;
        }
        try {
            Map<String, Object> response = imagesClient.get()
                    .uri("/search?q={q}&media_type=image&page_size=1", englishName + " planet solar system")
                    .retrieve()
                    .body(Map.class);
            String url = extractFirstImageUrl(response);
            if (url != null) {
                imageCache.put(englishName, new CachedImage(url, Instant.now().toEpochMilli()));
            }
            return url;
        } catch (Exception e) {
            return cached != null ? cached.url : null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractFirstImageUrl(Map<String, Object> response) {
        if (response == null) {
            return null;
        }
        Object collectionObj = response.get("collection");
        if (!(collectionObj instanceof Map<?, ?> collection)) {
            return null;
        }
        Object itemsObj = collection.get("items");
        if (!(itemsObj instanceof List<?> items) || items.isEmpty()) {
            return null;
        }
        Object first = items.get(0);
        if (!(first instanceof Map<?, ?> item)) {
            return null;
        }
        Object linksObj = item.get("links");
        if (!(linksObj instanceof List<?> links) || links.isEmpty()) {
            return null;
        }
        Object link0 = links.get(0);
        if (link0 instanceof Map<?, ?> link) {
            Object href = link.get("href");
            return href != null ? href.toString() : null;
        }
        return null;
    }

    private Map<String, Object> fallbackApod() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", "Pillars of Creation");
        map.put("explanation", "NASA 每日天文图暂时无法加载，请稍后再试。鹰状星云内的创生之柱是恒星诞生的标志性结构。");
        map.put("url", "https://www.nasa.gov/wp-content/uploads/2023/03/main_pillars_of_creation-m16-4029.jpg");
        map.put("media_type", "image");
        map.put("date", java.time.LocalDate.now().toString());
        map.put("copyright", "NASA, ESA, CSA, STScI");
        return map;
    }

    private List<PlanetDto> basePlanets() {
        return List.of(
                planet("mercury", "水星", "Mercury", "类地行星", "5790 万 km", "88 地球日", "4,879 km", "0", 1,
                        "太阳系最内侧行星，昼夜温差极大，几乎无大气层。"),
                planet("venus", "金星", "Venus", "类地行星", "1.08 亿 km", "225 地球日", "12,104 km", "0", 2,
                        "被浓厚二氧化碳大气包裹，温室效应极强，是太阳系最热的行星。"),
                planet("earth", "地球", "Earth", "类地行星", "1.50 亿 km", "365.25 日", "12,756 km", "1", 3,
                        "目前已知唯一存在稳定液态水与生命的行星，拥有保护性磁场与大气。"),
                planet("mars", "火星", "Mars", "类地行星", "2.28 亿 km", "687 地球日", "6,792 km", "2", 4,
                        "红色星球，拥有太阳系最大火山奥林匹斯山与峡谷水手号谷。"),
                planet("jupiter", "木星", "Jupiter", "气态巨行星", "7.78 亿 km", "11.9 地球年", "142,984 km", "95+", 5,
                        "太阳系最大行星，著名大红斑是持续数百年的巨型风暴。"),
                planet("saturn", "土星", "Saturn", "气态巨行星", "14.3 亿 km", "29.5 地球年", "120,536 km", "140+", 6,
                        "以壮观的光环系统闻名，主要成分为冰晶与岩石碎屑。"),
                planet("uranus", "天王星", "Uranus", "冰巨行星", "28.7 亿 km", "84 地球年", "51,118 km", "28", 7,
                        "自转轴几乎横躺，呈现独特的青绿色外观。"),
                planet("neptune", "海王星", "Neptune", "冰巨行星", "45.0 亿 km", "165 地球年", "49,528 km", "16", 8,
                        "太阳系风速最快的行星，通过数学计算预测后发现。")
        );
    }

    private PlanetDto planet(String id, String name, String nameEn, String type, String dist, String period,
                             String diameter, String moons, int order, String desc) {
        PlanetDto dto = new PlanetDto();
        dto.setId(id);
        dto.setName(name);
        dto.setNameEn(nameEn);
        dto.setType(type);
        dto.setDistanceFromSun(dist);
        dto.setOrbitalPeriod(period);
        dto.setDiameter(diameter);
        dto.setMoons(moons);
        dto.setOrder(order);
        dto.setDescription(desc);
        dto.setImageUrl(null);
        dto.setImageCredit("NASA");
        return dto;
    }

    private record CachedImage(String url, long cachedAt) {
        boolean isExpired() {
            return Instant.now().toEpochMilli() - cachedAt > CACHE_TTL_MS;
        }
    }

    private record CachedApod(Map<String, Object> data, long cachedAt) {
        boolean isExpired() {
            return Instant.now().toEpochMilli() - cachedAt > CACHE_TTL_MS;
        }
    }
}
