package com.springboot.backendserver.init;

import com.springboot.backendserver.entity.*;
import com.springboot.backendserver.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class QuizDataInitializer {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final BadgeRepository badgeRepository;

    public QuizDataInitializer(QuestionRepository questionRepository,
                               QuizRepository quizRepository,
                               QuizQuestionRepository quizQuestionRepository,
                               BadgeRepository badgeRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.badgeRepository = badgeRepository;
    }

    @PostConstruct
    public void seed() {
        if (quizRepository.count() > 0) {
            return;
        }

        Question q1 = saveQuestion(
                "太阳系中距离太阳最近的行星是？",
                QuestionType.SINGLE,
                "[{\"key\":\"A\",\"text\":\"水星\"},{\"key\":\"B\",\"text\":\"金星\"},{\"key\":\"C\",\"text\":\"地球\"},{\"key\":\"D\",\"text\":\"火星\"}]",
                "[\"A\"]",
                QuestionDifficulty.EASY,
                "水星是太阳系最内侧的行星。"
        );
        Question q2 = saveQuestion(
                "以下哪些属于气态巨行星？（多选）",
                QuestionType.MULTIPLE,
                "[{\"key\":\"A\",\"text\":\"木星\"},{\"key\":\"B\",\"text\":\"土星\"},{\"key\":\"C\",\"text\":\"火星\"},{\"key\":\"D\",\"text\":\"天王星\"}]",
                "[\"A\",\"B\",\"D\"]",
                QuestionDifficulty.MEDIUM,
                "木星、土星为气态巨行星；天王星、海王星常称冰巨行星，科普测验中一并计入气态巨行星类。"
        );
        Question q3 = saveQuestion(
                "光年是什么单位？",
                QuestionType.SINGLE,
                "[{\"key\":\"A\",\"text\":\"时间\"},{\"key\":\"B\",\"text\":\"距离\"},{\"key\":\"C\",\"text\":\"速度\"},{\"key\":\"D\",\"text\":\"亮度\"}]",
                "[\"B\"]",
                QuestionDifficulty.EASY,
                "光年是光在真空中一年传播的距离，是长度单位。"
        );
        Question q4 = saveQuestion(
                "银河系属于哪类天体系统？",
                QuestionType.SINGLE,
                "[{\"key\":\"A\",\"text\":\"恒星\"},{\"key\":\"B\",\"text\":\"星系\"},{\"key\":\"C\",\"text\":\"星云\"},{\"key\":\"D\",\"text\":\"星团\"}]",
                "[\"B\"]",
                QuestionDifficulty.EASY,
                "银河系是由数千亿颗恒星组成的棒旋星系。"
        );
        Question q5 = saveQuestion(
                "下列关于月球的说法正确的是？（多选）",
                QuestionType.MULTIPLE,
                "[{\"key\":\"A\",\"text\":\"月球围绕地球公转\"},{\"key\":\"B\",\"text\":\"月球有浓厚大气层\"},{\"key\":\"C\",\"text\":\"月相变化周期约29.5天\"},{\"key\":\"D\",\"text\":\"月球是太阳系最大卫星\"}]",
                "[\"A\",\"C\"]",
                QuestionDifficulty.MEDIUM,
                "月球几乎无大气；木卫三是太阳系最大卫星。"
        );

        Quiz quiz = new Quiz();
        quiz.setName("天文入门测验");
        quiz.setDescription("检验你对太阳系与基础天文概念的掌握程度，完成测验可解锁成就徽章。");
        quiz.setEnabled(true);
        quizRepository.save(quiz);

        long[] ids = {q1.getId(), q2.getId(), q3.getId(), q4.getId(), q5.getId()};
        for (int i = 0; i < ids.length; i++) {
            QuizQuestion link = new QuizQuestion();
            link.setQuizId(quiz.getId());
            link.setQuestionId(ids[i]);
            link.setSortOrder(i);
            quizQuestionRepository.save(link);
        }

        seedBadge("初窥门径", "🌟", "首次完成任意测验", BadgeRuleType.QUIZ_COUNT, 1);
        seedBadge("满分达人", "🏆", "单次测验得分达到 100 分", BadgeRuleType.QUIZ_SCORE, 100);
        seedBadge("勤学不辍", "🔥", "连续 3 天完成测验", BadgeRuleType.STREAK, 3);
        seedBadge("测验老手", "📚", "累计完成 5 次测验", BadgeRuleType.QUIZ_COUNT, 5);
    }

    private Question saveQuestion(String stem, QuestionType type, String optionsJson,
                                  String correctJson, QuestionDifficulty difficulty, String explanation) {
        Question q = new Question();
        q.setStem(stem);
        q.setType(type);
        q.setOptionsJson(optionsJson);
        q.setCorrectAnswerJson(correctJson);
        q.setDifficulty(difficulty);
        q.setExplanation(explanation);
        return questionRepository.save(q);
    }

    private void seedBadge(String name, String icon, String desc, BadgeRuleType ruleType, int value) {
        Badge badge = new Badge();
        badge.setName(name);
        badge.setIconUrl(icon);
        badge.setDescription(desc);
        badge.setRuleType(ruleType);
        badge.setRuleValue(value);
        badge.setEnabled(true);
        badgeRepository.save(badge);
    }
}
