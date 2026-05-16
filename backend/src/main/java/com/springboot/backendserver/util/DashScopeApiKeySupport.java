package com.springboot.backendserver.util;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Resolves and sanitizes DashScope / 百炼 API keys from config and environment.
 */
public final class DashScopeApiKeySupport {

    private static final String[] KEY_PROPERTY_NAMES = {
            "deepseek.api.key",
            "DASHSCOPE_API_KEY",
            "dashscope.api.key"
    };

    private DashScopeApiKeySupport() {
    }

    public static String resolve(Environment environment) {
        if (environment == null) {
            return "";
        }
        for (String name : KEY_PROPERTY_NAMES) {
            String value = environment.getProperty(name);
            String sanitized = sanitize(value);
            if (StringUtils.hasText(sanitized)) {
                return sanitized;
            }
        }
        return "";
    }

    public static String sanitize(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String key = raw.strip();
        if ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'"))) {
            key = key.substring(1, key.length() - 1).strip();
        }
        key = key.replace("\uFEFF", "").replace("\r", "").replace("\n", "");
        return key.strip();
    }

    public static String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "https://dashscope.aliyuncs.com/compatible-mode/v1";
        }
        String url = baseUrl.strip();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (url.endsWith("/v1")) {
            return url;
        }
        if (url.endsWith("/compatible-mode")) {
            return url + "/v1";
        }
        if (!url.contains("/compatible-mode")) {
            return url + "/compatible-mode/v1";
        }
        return url + "/v1";
    }

    public static String maskKey(String apiKey) {
        if (!StringUtils.hasText(apiKey) || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 3) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
