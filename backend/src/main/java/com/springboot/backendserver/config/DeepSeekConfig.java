package com.springboot.backendserver.config;

import com.springboot.backendserver.service.DeepSeekClient;
import com.springboot.backendserver.util.DashScopeApiKeySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

@Configuration
public class DeepSeekConfig {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekConfig.class);

    private static final Set<String> PLACEHOLDER_KEYS = Set.of(
            "",
            "your-deepseek-api-key",
            "sk-your-key-here",
            "changeme"
    );

    @Bean
    public DeepSeekClient deepSeekClient(
            Environment environment,
            @Qualifier("deepSeekRestClientBuilder") RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${deepseek.api.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
            @Value("${deepseek.model:qwen-plus}") String model) {
        String apiKey = DashScopeApiKeySupport.resolve(environment);
        if (!StringUtils.hasText(apiKey) || PLACEHOLDER_KEYS.contains(apiKey)) {
            throw new IllegalStateException(
                    "未配置有效的百炼 API Key。请在系统环境变量设置 DASHSCOPE_API_KEY，"
                            + "或在 application-local.properties 中设置 deepseek.api.key。"
                            + "Key 请从 https://bailian.console.aliyun.com/?tab=model#/api-key 创建");
        }
        if (!apiKey.startsWith("sk-")) {
            throw new IllegalStateException(
                    "百炼 API Key 格式不正确（应以 sk- 开头）。请从百炼控制台重新复制完整 Key");
        }
        String normalizedBaseUrl = DashScopeApiKeySupport.normalizeBaseUrl(baseUrl);
        log.info("DashScope AI 已启用，Key={}，baseUrl={}，model={}",
                DashScopeApiKeySupport.maskKey(apiKey), normalizedBaseUrl, normalizeModel(model));
        return new DeepSeekClient(
                restClientBuilder,
                objectMapper,
                apiKey,
                normalizedBaseUrl,
                normalizeModel(model));
    }

    static String normalizeModel(String model) {
        if (!StringUtils.hasText(model)) {
            return "qwen-plus";
        }
        return switch (model.trim()) {
            case "deepseek-chat", "deepseek-r1", "deepseek_r1", "r1" -> "qwen-plus";
            case "turbo" -> "qwen-turbo";
            case "max" -> "qwen-max";
            default -> model.trim();
        };
    }
}
