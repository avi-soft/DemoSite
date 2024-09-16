package com.community.api.services;

import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.boot.actuate.endpoint.Sanitizer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class SanitizerService {
    private final Sanitizer sanitizer = new Sanitizer();
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            ".*(select|insert|update|delete|union|drop|exec|create|alter|truncate|--|\\b'or\\b|\\b1=1\\b|\\b'\\s*or\\b|\\b'\\s*and\\b).*",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern XSS_PATTERN = Pattern.compile(
            ".*(<script|<img|onerror|javascript:|data:text/html|<iframe|<object|<embed).*",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern COMMAND_INJECTION_PATTERN = Pattern.compile(
            ".*(cmd.exe|bash|sh|exec|system|\\$\\(|\\;|\\||\\&|rm\\s+-rf|ls).*",
            Pattern.CASE_INSENSITIVE
    );
    public Map<String, Object> sanitizeInputMap(Map<String, Object> inputMap) {
        inputMap=removeKeyValuePair(inputMap);
        Map<String, Object> sanitizedDataMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            PropertySource<?> propertySource = new MapPropertySource("inputMapSource", inputMap);
            SanitizableData sanitizableData=new SanitizableData(propertySource,key,value);
            Object sanitizedValue = sanitizer.sanitize(sanitizableData);
            sanitizedDataMap.put(key, sanitizedValue);
        }
        return sanitizedDataMap;
    }
    public Map<String, Object> removeKeyValuePair(Map<String, Object> inputData) {
        Map<String, Object> cleanedData = new HashMap<>(inputData);
        Iterator<Map.Entry<String, Object>> iterator = cleanedData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value != null && isMalicious(value.toString())) {
                iterator.remove();
            }
        }

        return cleanedData;
    }

    private boolean isMalicious(String value) {
        return SQL_INJECTION_PATTERN.matcher(value).matches() ||
                XSS_PATTERN.matcher(value).matches() ||
                COMMAND_INJECTION_PATTERN.matcher(value).matches();
    }
}