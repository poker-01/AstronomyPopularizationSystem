package com.springboot.backendserver.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.springboot.backendserver.common.BusinessException;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DeepSeekClient {

    private static final String SYSTEM_PROMPT = """
            你是「COSMOS科普」天文助手，专门解答天文学、航天、宇宙、太阳系与观测相关问题。
            请用简洁、准确、易懂的中文回答；若问题与天文无关，请礼貌说明并引导用户提问天文话题。
            不确定时请如实说明，不要编造科学事实。
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public DeepSeekClient(RestClient.Builder restClientBuilder,
                          ObjectMapper objectMapper,
                          String apiKey,
                          String baseUrl,
                          String model) {
        this.objectMapper = objectMapper;
        this.model = model;
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @SuppressWarnings("unchecked")
    public String chat(List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        messages.addAll(history);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messages", messages);
        body.put("stream", false);

        List<String> modelsToTry = fallbackModels(model);
        RestClientResponseException lastError = null;

        for (String modelName : modelsToTry) {
            body.put("model", modelName);
            try {
                Map<String, Object> response = restClient.post()
                        .uri("/chat/completions")
                        .body(body)
                        .retrieve()
                        .body(Map.class);

                String content = extractContent(response);
                if (!StringUtils.hasText(content)) {
                    throw BusinessException.badRequest("AI 返回内容为空");
                }
                return sanitizeReply(content);
            } catch (RestClientResponseException ex) {
                lastError = ex;
                if (ex.getStatusCode().value() != 400) {
                    break;
                }
            } catch (BusinessException ex) {
                throw ex;
            } catch (Exception ex) {
                throw BusinessException.badRequest("AI 服务暂时不可用，请稍后重试");
            }
        }

        if (lastError != null) {
            throw BusinessException.badRequest("AI 服务调用失败：" + describeApiError(lastError));
        }
        throw BusinessException.badRequest("AI 服务暂时不可用，请稍后重试");
    }

    public void chatStream(List<Map<String, String>> history, Consumer<String> onDelta) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        messages.addAll(history);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messages", messages);
        body.put("stream", true);

        List<String> modelsToTry = fallbackModels(model);
        RestClientResponseException lastError = null;

        for (String modelName : modelsToTry) {
            body.put("model", modelName);
            try {
                restClient.post()
                        .uri("/chat/completions")
                        .body(body)
                        .exchange((request, response) -> {
                            try {
                                if (!response.getStatusCode().is2xxSuccessful()) {
                                    String errBody = new String(
                                            response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                                    throw new RestClientResponseException(
                                            "AI stream error",
                                            response.getStatusCode().value(),
                                            response.getStatusCode().toString(),
                                            response.getHeaders(),
                                            errBody.getBytes(StandardCharsets.UTF_8),
                                            StandardCharsets.UTF_8);
                                }
                                readSseStream(response.getBody(), onDelta);
                                return null;
                            } catch (IOException ex) {
                                throw new UncheckedIOException(ex);
                            }
                        });
                return;
            } catch (RestClientResponseException ex) {
                lastError = ex;
                if (ex.getStatusCode().value() != 400) {
                    break;
                }
            } catch (BusinessException ex) {
                throw ex;
            } catch (Exception ex) {
                throw BusinessException.badRequest("AI 服务暂时不可用，请稍后重试");
            }
        }

        if (lastError != null) {
            throw BusinessException.badRequest("AI 服务调用失败：" + describeApiError(lastError));
        }
        throw BusinessException.badRequest("AI 服务暂时不可用，请稍后重试");
    }

    public String finalizeStreamedContent(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw BusinessException.badRequest("AI 返回内容为空");
        }
        return sanitizeReply(raw);
    }

    private void readSseStream(java.io.InputStream inputStream, Consumer<String> onDelta) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if (data.isEmpty() || "[DONE]".equals(data)) {
                    continue;
                }
                String delta = parseStreamDelta(data);
                if (StringUtils.hasText(delta)) {
                    onDelta.accept(delta);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String parseStreamDelta(String jsonLine) {
        try {
            JsonNode root = objectMapper.readTree(jsonLine);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }
            JsonNode delta = choices.get(0).path("delta");
            String content = jsonText(delta.path("content"));
            if (StringUtils.hasText(content)) {
                return content;
            }
            return jsonText(delta.path("reasoning_content"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> fallbackModels(String primary) {
        Set<String> ordered = new LinkedHashSet<>();
        ordered.add(primary);
        ordered.add("qwen-plus");
        ordered.add("qwen-turbo");
        return List.copyOf(ordered);
    }

    private String describeApiError(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        String apiMessage = parseErrorMessage(ex.getResponseBodyAsString());
        if (StringUtils.hasText(apiMessage)) {
            return apiMessage;
        }
        return switch (status) {
            case 401 -> "百炼 API Key 无效或已失效，请到百炼控制台重新创建并更新 DASHSCOPE_API_KEY 后重启后端";
            case 402 -> "账户余额不足，请登录阿里云百炼控制台充值";
            case 403 -> "无权访问该模型或接口，请检查账户权限";
            case 429 -> "请求过于频繁，请稍后再试";
            case 500, 502, 503, 504 -> "AI 服务暂时异常（HTTP " + status + "），请稍后重试";
            default -> "HTTP " + status
                    + (StringUtils.hasText(ex.getStatusText()) ? " " + ex.getStatusText() : "");
        };
    }

    private String parseErrorMessage(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode error = root.path("error");
            if (error.isObject()) {
                String message = jsonText(error.path("message"));
                if (StringUtils.hasText(message)) {
                    return message;
                }
                String code = jsonText(error.path("code"));
                if (StringUtils.hasText(code)) {
                    return code;
                }
            }
            String message = jsonText(root.path("message"));
            if (StringUtils.hasText(message)) {
                return message;
            }
        } catch (Exception ignored) {
            if (body.length() <= 200) {
                return body.trim();
            }
        }
        return null;
    }

    private static String jsonText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String text = node.asString();
        return StringUtils.hasText(text) ? text : null;
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        if (response == null) {
            return null;
        }
        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return null;
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> choice)) {
            return null;
        }
        Object messageObj = choice.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) {
            return null;
        }
        Object content = message.get("content");
        if (content != null && StringUtils.hasText(content.toString())) {
            return content.toString();
        }
        Object reasoning = message.get("reasoning_content");
        return reasoning != null ? reasoning.toString() : null;
    }

    private String sanitizeReply(String content) {
        String cleaned = stripThinkBlocks(content).trim();
        return cleaned.isEmpty() ? content.trim() : cleaned;
    }

    private String stripThinkBlocks(String text) {
        final String open = "\u003cthink\u003e";
        final String close = "\u003c/think\u003e";
        StringBuilder sb = new StringBuilder(text);
        int idx;
        while ((idx = sb.indexOf(open)) >= 0) {
            int end = sb.indexOf(close, idx);
            if (end < 0) {
                sb.delete(idx, sb.length());
                break;
            }
            sb.delete(idx, end + close.length());
        }
        return sb.toString();
    }
}
