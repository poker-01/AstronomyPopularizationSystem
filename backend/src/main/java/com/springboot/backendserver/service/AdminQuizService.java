package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.entity.Quiz;
import com.springboot.backendserver.entity.QuizQuestion;
import com.springboot.backendserver.repository.QuestionRepository;
import com.springboot.backendserver.repository.QuizQuestionRepository;
import com.springboot.backendserver.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminQuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuestionRepository questionRepository;

    public AdminQuizService(QuizRepository quizRepository,
                            QuizQuestionRepository quizQuestionRepository,
                            QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.questionRepository = questionRepository;
    }

    public List<QuizSummaryDto> list() {
        return quizRepository.findAll().stream()
                .map(q -> QuizSummaryDto.from(q, (int) quizQuestionRepository.countByQuizId(q.getId())))
                .toList();
    }

    public QuizDetailDto get(Long id) {
        Quiz quiz = findOrThrow(id);
        List<Long> questionIds = quizQuestionRepository.findByQuizIdOrderBySortOrderAsc(id).stream()
                .map(QuizQuestion::getQuestionId)
                .toList();
        Map<Long, QuestionDto> qMap = questionRepository.findAllById(questionIds).stream()
                .map(QuestionDto::from)
                .collect(java.util.stream.Collectors.toMap(QuestionDto::getId, q -> q));
        List<QuestionDto> questions = questionIds.stream()
                .map(qMap::get)
                .filter(java.util.Objects::nonNull)
                .toList();
        return QuizDetailDto.from(quiz, questionIds, questions);
    }

    @Transactional
    public QuizDetailDto create(QuizSaveRequest request) {
        validate(request);
        Quiz quiz = new Quiz();
        apply(quiz, request);
        quizRepository.save(quiz);
        syncQuestions(quiz.getId(), request.getQuestionIds());
        return get(quiz.getId());
    }

    @Transactional
    public QuizDetailDto update(Long id, QuizSaveRequest request) {
        Quiz quiz = findOrThrow(id);
        validate(request);
        apply(quiz, request);
        quizRepository.save(quiz);
        syncQuestions(id, request.getQuestionIds());
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        if (!quizRepository.existsById(id)) {
            throw BusinessException.notFound("测验不存在");
        }
        quizQuestionRepository.deleteByQuizId(id);
        quizRepository.deleteById(id);
    }

    private Quiz findOrThrow(Long id) {
        return quizRepository.findById(id).orElseThrow(() -> BusinessException.notFound("测验不存在"));
    }

    private void validate(QuizSaveRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw BusinessException.badRequest("测验名称不能为空");
        }
    }

    private void apply(Quiz quiz, QuizSaveRequest request) {
        quiz.setName(request.getName().trim());
        quiz.setDescription(request.getDescription());
        if (request.getEnabled() != null) {
            quiz.setEnabled(request.getEnabled());
        }
    }

    private void syncQuestions(Long quizId, List<Long> questionIds) {
        quizQuestionRepository.deleteByQuizId(quizId);
        if (questionIds == null || questionIds.isEmpty()) {
            return;
        }
        int order = 0;
        for (Long qid : questionIds) {
            if (!questionRepository.existsById(qid)) {
                throw BusinessException.badRequest("题目不存在: " + qid);
            }
            QuizQuestion link = new QuizQuestion();
            link.setQuizId(quizId);
            link.setQuestionId(qid);
            link.setSortOrder(order++);
            quizQuestionRepository.save(link);
        }
    }
}
