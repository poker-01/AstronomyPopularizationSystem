package com.springboot.backendserver.util;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.springboot.backendserver.common.BusinessException;

import java.util.*;

public final class JsonAnswerUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonAnswerUtils() {
    }

    public static List<Map<String, String>> parseOptions(String optionsJson) {
        try {
            return MAPPER.readValue(optionsJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw BusinessException.badRequest("选项 JSON 格式无效");
        }
    }

    public static Set<String> parseAnswerKeys(String json) {
        try {
            List<String> keys = MAPPER.readValue(json, new TypeReference<>() {});
            return new TreeSet<>(keys);
        } catch (Exception e) {
            throw BusinessException.badRequest("正确答案 JSON 格式无效，应为字符串数组如 [\"A\"]");
        }
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw BusinessException.badRequest("JSON 序列化失败");
        }
    }

    public static boolean answersMatch(String correctJson, List<String> userAnswer) {
        Set<String> correct = parseAnswerKeys(correctJson);
        Set<String> given = userAnswer == null ? Set.of() : new TreeSet<>(userAnswer);
        return correct.equals(given);
    }
}
