package com.springboot.backendserver.init;

import com.springboot.backendserver.entity.*;
import com.springboot.backendserver.repository.ArticleRepository;
import com.springboot.backendserver.repository.CategoryRepository;
import com.springboot.backendserver.repository.ExplorationEventRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
@Order(2)
public class ContentDataInitializer {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;
    private final ExplorationEventRepository explorationEventRepository;

    public ContentDataInitializer(CategoryRepository categoryRepository,
                                  ArticleRepository articleRepository,
                                  ExplorationEventRepository explorationEventRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
        this.explorationEventRepository = explorationEventRepository;
    }

    @PostConstruct
    public void seed() {
        seedCategories();
        seedArticles();
        seedExplorationEvents();
    }

    private void seedCategories() {
        saveCategory("solar-system", "太阳系", "行星、卫星与太阳系的形成与演化", 1);
        saveCategory("deep-space", "深空天体", "星系、星云、黑洞与宇宙结构", 2);
        saveCategory("observation", "观测指南", "业余天文观测技巧与设备入门", 3);
    }

    private Category saveCategory(String slug, String name, String desc, int order) {
        return categoryRepository.findBySlug(slug).orElseGet(() -> {
            Category c = new Category();
            c.setSlug(slug);
            c.setName(name);
            c.setDescription(desc);
            c.setSortOrder(order);
            return categoryRepository.save(c);
        });
    }

    private void seedArticles() {
        if (articleRepository.count() > 0) {
            return;
        }
        Category solar = categoryRepository.findBySlug("solar-system").orElseThrow();
        Category deep = categoryRepository.findBySlug("deep-space").orElseThrow();
        Category obs = categoryRepository.findBySlug("observation").orElseThrow();

        createArticle(solar.getId(), "太阳系是如何形成的？", "solar-system-formation",
                "从星云到行星：太阳系的诞生",
                "https://www.nasa.gov/wp-content/uploads/2023/03/main_pillars_of_creation-m16-4029.jpg",
                """
                ## 太阳系的起源
                
                约 46 亿年前，一片巨大的分子云在引力作用下坍缩，中心形成了原恒星——我们的太阳。周围残留的气体与尘埃形成原行星盘，尘埃颗粒碰撞粘连，逐渐长成行星胚胎。
                
                ### 内太阳系与外太阳系
                
                - **内太阳系**：高温使轻物质难以凝聚，形成了岩石为主的类地行星（水星、金星、地球、火星）。
                - **外太阳系**：低温允许冰与气体富集，形成了气态巨行星与冰巨行星（木星、土星、天王星、海王星）。
                
                ### 为什么这很重要？
                
                理解太阳系形成过程，有助于我们在其他恒星周围寻找类似地球的环境，并解释行星大气、卫星与彗星等天体的分布规律。
                """,
                ArticleStatus.PUBLISHED);

        createArticle(solar.getId(), "什么是宜居带？", "habitable-zone",
                "恒星周围生命可能存在的黄金地带",
                "https://www.nasa.gov/wp-content/uploads/2021/07/Kepler-186f.jpg",
                """
                ## 宜居带（Habitable Zone）
                
                宜居带指恒星周围温度适中、行星表面可能存在液态水的轨道范围。对太阳而言，地球正好位于宜居带内缘附近。
                
                ### 关键条件
                
                1. 液态水可长期稳定存在
                2. 大气层可保持适宜压力与温室效应
                3. 磁场与板块活动有助于长期气候稳定（地球特例）
                
                NASA 的 **开普勒** 与 **TESS** 任务已发现数千颗系外行星，其中多颗位于各自恒星的宜居带内。
                """,
                ArticleStatus.PUBLISHED);

        createArticle(deep.getId(), "黑洞：时空的极端弯曲", "black-holes-intro",
                "从史瓦西半径到事件视界",
                "https://www.nasa.gov/wp-content/uploads/2019/04/blackhole.png",
                """
                ## 黑洞基础
                
                当大质量恒星耗尽燃料坍缩，或星系中心吸积足够物质时，可能形成黑洞——引力强到连光都无法逃逸的区域。
                
                ### 事件视界
                
                事件视界是黑洞的「边界」，一旦越过，任何信息都无法返回。2019 年，**事件视界望远镜（EHT）** 首次拍摄到 M87 星系中心黑洞的「阴影」图像，验证了广义相对论的预言。
                
                NASA 的钱德拉 X 射线天文台与詹姆斯·韦布空间望远镜持续帮助科学家研究黑洞周围吸积盘与喷流。
                """,
                ArticleStatus.PUBLISHED);

        createArticle(obs.getId(), "初学者如何观测星空？", "beginner-stargazing",
                "从肉眼到双筒望远镜的入门路线",
                "https://www.nasa.gov/wp-content/uploads/2023/10/nasa-logo-web-rgb.png",
                """
                ## 业余观测入门
                
                1. **选择暗空地点**：远离城市光害，使用 [NASA 暗空地图](https://www.lightpollutionmap.info/) 辅助选址。
                2. **从星座开始**：先认识北斗七星、猎户座等标志星座，建立天空坐标感。
                3. **月相规划**：新月前后最适合观测深空天体；满月适合观测月面环形山。
                4. **善用 NASA 资源**：APOD（每日天文图）与 [Solar System Exploration](https://science.nasa.gov/solar-system/) 提供最新科普与任务动态。
                """,
                ArticleStatus.PUBLISHED);
    }

    private void createArticle(Long categoryId, String title, String slug, String summary,
                               String coverUrl, String content, ArticleStatus status) {
        Article article = new Article();
        article.setCategoryId(categoryId);
        article.setTitle(title);
        article.setSlug(slug);
        article.setSummary(summary);
        article.setCoverUrl(coverUrl);
        article.setContent(content);
        article.setStatus(status);
        article.setViewCount(0L);
        article.setPublishedAt(LocalDateTime.now());
        articleRepository.save(article);
    }

    private void seedExplorationEvents() {
        if (explorationEventRepository.count() > 0) {
            return;
        }
        int order = 0;
        saveEvent(1957, "10月", "斯普特尼克 1 号", "人造卫星", order++,
                "苏联发射人类第一颗人造卫星，开启太空时代。",
                "https://www.nasa.gov/wp-content/uploads/2023/03/524943main_sputnik-1.jpg");
        saveEvent(1961, "4月", "尤里·加加林轨道飞行", "载人航天", order++,
                "加加林搭乘东方一号完成人类首次绕地轨道飞行。",
                "https://www.nasa.gov/wp-content/uploads/2021/04/gagarin.jpg");
        saveEvent(1969, "7月", "阿波罗 11 号登月", "载人登月", order++,
                "阿姆斯特朗与奥尔德林首次踏上月球表面，「这是个人的一小步，却是人类的一大步」。",
                "https://www.nasa.gov/wp-content/uploads/2023/07/apollo11-as11-40-5874.jpg");
        saveEvent(1977, "8月", "旅行者 1/2 号发射", "深空探测", order++,
                "双探测器探访木星、土星等外行星，旅行者 1 号现已成为最遥远的人造物体。",
                "https://www.nasa.gov/wp-content/uploads/2018/04/voyager-1.jpg");
        saveEvent(1990, "4月", "哈勃空间望远镜升空", "空间望远镜", order++,
                "哈勃彻底改变人类对宇宙距离、星系演化与系外行星的研究方式。",
                "https://www.nasa.gov/wp-content/uploads/2023/03/hubble-space-telescope.jpg");
        saveEvent(1998, "11月", "国际空间站首个舱段发射", "空间站", order++,
                "ISS 成为多国合作的长期轨道实验室，持续运行至今。",
                "https://www.nasa.gov/wp-content/uploads/2023/03/iss.jpg");
        saveEvent(2012, "8月", "好奇号火星车着陆", "行星探测", order++,
                "好奇号在盖尔陨石坑着陆，探索火星古代宜居环境证据。",
                "https://www.nasa.gov/wp-content/uploads/2023/03/curiosity-rover.jpg");
        saveEvent(2021, "12月", "詹姆斯·韦布空间望远镜发射", "空间望远镜", order++,
                "韦布在红外波段观测早期宇宙、系外行星大气与恒星形成区。",
                "https://www.nasa.gov/wp-content/uploads/2022/07/webb-first-images-release.jpg");
        saveEvent(2024, "10月", "欧罗巴快船发射", "行星探测", order,
                "NASA 任务前往木卫二，研究冰下海洋与生命潜在宜居性。",
                "https://www.nasa.gov/wp-content/uploads/2024/10/europa-clipper.jpg");
    }

    private void saveEvent(int year, String month, String title, String category, int sortOrder,
                           String description, String imageUrl) {
        ExplorationEvent event = new ExplorationEvent();
        event.setYear(year);
        event.setMonth(month);
        event.setTitle(title);
        event.setCategory(category);
        event.setDescription(description);
        event.setImageUrl(imageUrl);
        event.setSortOrder(sortOrder);
        explorationEventRepository.save(event);
    }
}
