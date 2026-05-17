package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.context.AuthContext;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.entity.Question;
import com.springboot.backendserver.entity.Quiz;
import com.springboot.backendserver.entity.QuizQuestion;
import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.entity.UserQuizAttempt;
import com.springboot.backendserver.repository.QuestionRepository;
import com.springboot.backendserver.repository.QuizQuestionRepository;
import com.springboot.backendserver.repository.QuizRepository;
import com.springboot.backendserver.repository.UserQuizAttemptRepository;
import com.springboot.backendserver.util.JsonAnswerUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuestionRepository questionRepository;
    private final UserQuizAttemptRepository attemptRepository;
    private final BadgeService badgeService;

    public QuizService(QuizRepository quizRepository,
                       QuizQuestionRepository quizQuestionRepository,
                       QuestionRepository questionRepository,
                       UserQuizAttemptRepository attemptRepository,
                       BadgeService badgeService) {
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.badgeService = badgeService;
    }

    public List<QuizSummaryDto> listQuizzes() {
        return quizRepository.findByEnabledTrueOrderByIdAsc().stream()
                .map(q -> QuizSummaryDto.from(q, (int) quizQuestionRepository.countByQuizId(q.getId())))
                .toList();
    }

    public QuizPublicDto getQuizForPlay(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> BusinessException.notFound("测验不存在"));
        if (!Boolean.TRUE.equals(quiz.getEnabled())) {
            throw BusinessException.notFound("测验不存在");
        }
        List<Question> questions = loadQuizQuestions(quizId);
        if (questions.isEmpty()) {
            throw BusinessException.badRequest("该测验暂无题目");
        }
        List<QuestionPublicDto> publicQuestions = questions.stream().map(QuestionPublicDto::from).toList();
        return QuizPublicDto.from(quiz, publicQuestions);
    }

    @Transactional
    public QuizSubmitResultDto submit(Long quizId, QuizSubmitRequest request) {
        User user = AuthContext.require();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> BusinessException.notFound("测验不存在"));
        if (!Boolean.TRUE.equals(quiz.getEnabled())) {
            throw BusinessException.notFound("测验不存在");
        }

        List<Question> questions = loadQuizQuestions(quizId);
        if (questions.isEmpty()) {
            throw BusinessException.badRequest("该测验暂无题目");
        }

        Map<String, List<String>> answers = request != null && request.getAnswers() != null
                ? request.getAnswers() : Map.of();

        int correct = 0;
        Map<Long, QuizSubmitResultDto.QuestionResultItem> results = new LinkedHashMap<>();
        for (Question q : questions) {
            List<String> userKeys = answers.getOrDefault(String.valueOf(q.getId()), List.of());
            boolean ok = JsonAnswerUtils.answersMatch(q.getCorrectAnswerJson(), userKeys);
            if (ok) {
                correct++;
            }
            QuizSubmitResultDto.QuestionResultItem item = new QuizSubmitResultDto.QuestionResultItem();
            item.setCorrect(ok);
            item.setCorrectAnswer(JsonAnswerUtils.parseAnswerKeys(q.getCorrectAnswerJson()).stream().toList());
            item.setExplanation(q.getExplanation());
            results.put(q.getId(), item);
        }

        int total = questions.size();
        int percent = total == 0 ? 0 : (correct * 100) / total;

        UserQuizAttempt attempt = new UserQuizAttempt();
        attempt.setUserId(user.getId());
        attempt.setQuizId(quizId);
        attempt.setScore(percent);
        attempt.setAnswersJson(JsonAnswerUtils.toJson(answers));
        attemptRepository.save(attempt);

        List<UserBadgeDto> newBadges = badgeService.evaluateAndAward(user.getId(), percent);

        QuizSubmitResultDto result = new QuizSubmitResultDto();
        result.setScore(correct);
        result.setTotal(total);
        result.setPercent(percent);
        result.setAttemptId(attempt.getId());
        result.setResults(results);
        result.setNewBadges(newBadges);
        return result;
    }

    private List<Question> loadQuizQuestions(Long quizId) {
        List<QuizQuestion> links = quizQuestionRepository.findByQuizIdOrderBySortOrderAsc(quizId);
        if (links.isEmpty()) {
            return List.of();
        }
        List<Long> ids = links.stream().map(QuizQuestion::getQuestionId).toList();
        Map<Long, Question> map = questionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        return ids.stream().map(map::get).filter(Objects::nonNull).toList();
    }
}
