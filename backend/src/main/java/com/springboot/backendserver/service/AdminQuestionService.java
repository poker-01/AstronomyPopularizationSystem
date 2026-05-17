package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.entity.Question;
import com.springboot.backendserver.entity.QuestionDifficulty;
import com.springboot.backendserver.entity.QuestionType;
import com.springboot.backendserver.repository.QuestionRepository;
import com.springboot.backendserver.util.JsonAnswerUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminQuestionService {

    private final QuestionRepository questionRepository;

    public AdminQuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public PageResult<QuestionDto> list(int page, int size, String keyword, String difficulty) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Specification<Question> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("stem")), pattern));
            }
            if (StringUtils.hasText(difficulty)) {
                try {
                    predicates.add(cb.equal(root.get("difficulty"), QuestionDifficulty.valueOf(difficulty)));
                } catch (IllegalArgumentException ignored) {
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Question> result = questionRepository.findAll(
                spec,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );
        List<QuestionDto> content = result.getContent().stream().map(QuestionDto::from).toList();
        return PageResult.of(content, result.getTotalElements(), result.getTotalPages(), safePage, safeSize);
    }

    public QuestionDto get(Long id) {
        return QuestionDto.from(findOrThrow(id));
    }

    @Transactional
    public QuestionDto create(QuestionSaveRequest request) {
        validate(request);
        Question q = new Question();
        apply(q, request);
        questionRepository.save(q);
        return QuestionDto.from(q);
    }

    @Transactional
    public QuestionDto update(Long id, QuestionSaveRequest request) {
        Question q = findOrThrow(id);
        validate(request);
        apply(q, request);
        questionRepository.save(q);
        return QuestionDto.from(q);
    }

    @Transactional
    public void delete(Long id) {
        if (!questionRepository.existsById(id)) {
            throw BusinessException.notFound("题目不存在");
        }
        questionRepository.deleteById(id);
    }

    @Transactional
    public int importBatch(QuestionImportRequest request) {
        if (request == null || request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw BusinessException.badRequest("导入列表不能为空");
        }
        int count = 0;
        for (QuestionSaveRequest item : request.getQuestions()) {
            validate(item);
            Question q = new Question();
            apply(q, item);
            questionRepository.save(q);
            count++;
        }
        return count;
    }

    private Question findOrThrow(Long id) {
        return questionRepository.findById(id).orElseThrow(() -> BusinessException.notFound("题目不存在"));
    }

    private void validate(QuestionSaveRequest request) {
        if (request == null || !StringUtils.hasText(request.getStem())) {
            throw BusinessException.badRequest("题干不能为空");
        }
        if (request.getOptions() == null || request.getOptions().isEmpty()) {
            throw BusinessException.badRequest("选项不能为空");
        }
        if (request.getCorrectAnswer() == null || request.getCorrectAnswer().isEmpty()) {
            throw BusinessException.badRequest("正确答案不能为空");
        }
        try {
            QuestionType.valueOf(request.getType());
        } catch (Exception e) {
            throw BusinessException.badRequest("题型须为 SINGLE 或 MULTIPLE");
        }
    }

    private void apply(Question q, QuestionSaveRequest request) {
        q.setStem(request.getStem().trim());
        q.setType(QuestionType.valueOf(request.getType()));
        q.setOptionsJson(JsonAnswerUtils.toJson(request.getOptions()));
        q.setCorrectAnswerJson(JsonAnswerUtils.toJson(request.getCorrectAnswer()));
        if (StringUtils.hasText(request.getDifficulty())) {
            q.setDifficulty(QuestionDifficulty.valueOf(request.getDifficulty()));
        } else if (q.getDifficulty() == null) {
            q.setDifficulty(QuestionDifficulty.MEDIUM);
        }
        q.setExplanation(request.getExplanation());
    }
}
