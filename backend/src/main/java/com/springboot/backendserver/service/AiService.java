package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.context.AuthContext;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.entity.AiMessage;
import com.springboot.backendserver.entity.AiSession;
import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.repository.AiMessageRepository;
import com.springboot.backendserver.repository.AiSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final int MAX_HISTORY_MESSAGES = 20;

    private static final long SSE_TIMEOUT_MS = 120_000L;

    private final AiSessionRepository sessionRepository;
    private final AiMessageRepository messageRepository;
    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public AiService(AiSessionRepository sessionRepository,
                     AiMessageRepository messageRepository,
                     DeepSeekClient deepSeekClient,
                     ObjectMapper objectMapper,
                     org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.deepSeekClient = deepSeekClient;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional
    public AiChatResponse chat(AiChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw BusinessException.badRequest("请输入问题");
        }
        String userText = request.getMessage().trim();
        if (userText.length() > 2000) {
            throw BusinessException.badRequest("问题长度不能超过 2000 字");
        }

        User user = AuthContext.require();
        AiSession session = resolveSession(user.getId(), request.getSessionId());

        saveMessage(session.getId(), "user", userText);

        List<AiMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        List<Map<String, String>> apiMessages = history.stream()
                .skip(Math.max(0, history.size() - MAX_HISTORY_MESSAGES))
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList());

        String reply = deepSeekClient.chat(apiMessages);
        saveMessage(session.getId(), "assistant", reply);

        if (!StringUtils.hasText(session.getTitle())) {
            session.setTitle(truncateTitle(userText));
            sessionRepository.save(session);
        } else {
            sessionRepository.save(session);
        }

        List<AiMessageDto> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
                .map(AiMessageDto::from)
                .toList();

        AiChatResponse response = new AiChatResponse();
        response.setSessionId(session.getId());
        response.setReply(reply);
        response.setMessages(messages);
        return response;
    }

    public SseEmitter chatStream(AiChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw BusinessException.badRequest("请输入问题");
        }
        String userText = request.getMessage().trim();
        if (userText.length() > 2000) {
            throw BusinessException.badRequest("问题长度不能超过 2000 字");
        }

        User user = AuthContext.require();
        AiSession session = resolveSession(user.getId(), request.getSessionId());
        saveMessage(session.getId(), "user", userText);

        List<AiMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        List<Map<String, String>> apiMessages = history.stream()
                .skip(Math.max(0, history.size() - MAX_HISTORY_MESSAGES))
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList());

        Long sessionId = session.getId();
        boolean needsTitle = !StringUtils.hasText(session.getTitle());
        String titleSeed = userText;

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        Thread streamThread = new Thread(() -> runStream(emitter, sessionId, needsTitle, titleSeed, apiMessages),
                "ai-chat-stream-" + sessionId);
        streamThread.setDaemon(true);
        streamThread.start();
        return emitter;
    }

    private void runStream(SseEmitter emitter,
                           Long sessionId,
                           boolean needsTitle,
                           String titleSeed,
                           List<Map<String, String>> apiMessages) {
        StringBuilder buffer = new StringBuilder();
        try {
            sendEvent(emitter, "meta", Map.of("sessionId", sessionId));
            deepSeekClient.chatStream(apiMessages, delta -> {
                buffer.append(delta);
                try {
                    sendEvent(emitter, "delta", Map.of("content", delta));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            String reply = deepSeekClient.finalizeStreamedContent(buffer.toString());
            AiChatResponse done = transactionTemplate.execute(status ->
                    finalizeStreamReply(sessionId, needsTitle, titleSeed, reply));

            sendEvent(emitter, "done", done);
            emitter.complete();
        } catch (BusinessException ex) {
            completeWithError(emitter, ex.getMessage());
        } catch (Exception ex) {
            completeWithError(emitter, "AI 服务暂时不可用，请稍后重试");
        }
    }

    private AiChatResponse finalizeStreamReply(Long sessionId, boolean needsTitle, String titleSeed, String reply) {
        saveMessage(sessionId, "assistant", reply);

        AiSession session = sessionRepository.findById(sessionId).orElseThrow();
        if (needsTitle) {
            session.setTitle(truncateTitle(titleSeed));
        }
        sessionRepository.save(session);

        List<AiMessageDto> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(AiMessageDto::from)
                .toList();

        AiChatResponse response = new AiChatResponse();
        response.setSessionId(sessionId);
        response.setReply(reply);
        response.setMessages(messages);
        return response;
    }

    private void sendEvent(SseEmitter emitter, String name, Object payload) throws IOException {
        emitter.send(SseEmitter.event()
                .name(name)
                .data(objectMapper.writeValueAsString(payload)));
    }

    private void completeWithError(SseEmitter emitter, String message) {
        try {
            sendEvent(emitter, "error", Map.of("message", message));
        } catch (IOException ignored) {
            /* client may have disconnected */
        }
        emitter.complete();
    }

    public List<AiSessionDto> listSessions() {
        Long userId = AuthContext.require().getId();
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(AiSessionDto::from)
                .toList();
    }

    public AiSessionDto getSession(Long sessionId) {
        User user = AuthContext.require();
        AiSession session = sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> BusinessException.notFound("会话不存在"));
        AiSessionDto dto = AiSessionDto.from(session);
        dto.setMessages(messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(AiMessageDto::from)
                .toList());
        return dto;
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        User user = AuthContext.require();
        AiSession session = sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> BusinessException.notFound("会话不存在"));
        messageRepository.deleteBySessionId(session.getId());
        sessionRepository.delete(session);
    }

    @Transactional
    public AiSessionDto newSession() {
        User user = AuthContext.require();
        AiSession session = new AiSession();
        session.setUserId(user.getId());
        session.setTitle(null);
        sessionRepository.save(session);
        return AiSessionDto.from(session);
    }

    private AiSession resolveSession(Long userId, Long sessionId) {
        if (sessionId == null) {
            AiSession session = new AiSession();
            session.setUserId(userId);
            return sessionRepository.save(session);
        }
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> BusinessException.notFound("会话不存在"));
    }

    private void saveMessage(Long sessionId, String role, String content) {
        AiMessage message = new AiMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        messageRepository.save(message);
    }

    private String truncateTitle(String text) {
        return text.length() > 24 ? text.substring(0, 24) + "…" : text;
    }
}
