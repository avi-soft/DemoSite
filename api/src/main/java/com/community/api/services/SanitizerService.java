package com.community.api.services;
import com.community.api.component.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(SanitizerService.class);
    private final Sanitizer sanitizer = new Sanitizer();
/*    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            ".*(select|insert|update|delete|union|drop|exec|create|alter|truncate|--|\\b'or\\b|\\b1=1\\b|\\b'\\s*or\\b|\\b'\\s*and\\b).*",
            Pattern.CASE_INSENSITIVE
    );*/
        private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
                ".*\\b(select|insert|update|delete|union|drop|exec|create|alter|truncate)\\b.*",
                Pattern.CASE_INSENSITIVE
        );
    private static final Pattern FILE_INCLUSION_PATTERN = Pattern.compile(
            ".*(file://|php://|ftp://|http://|https://|jar://|zip://|dict://|gopher://|ws://|wss://|mailto:|data:|javascript:).*",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HTML_INJECTION_PATTERN = Pattern.compile(
            ".*(<[^>]*>).*",
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


    public boolean isMalicious(String value) {
        boolean isMalicious = false;

        if (SQL_INJECTION_PATTERN.matcher(value).find()) {
            isMalicious = true;
            logger.info("SQL Injection detected");
        }
        if (XSS_PATTERN.matcher(value).find()) {
            isMalicious = true;
            logger.info("XSS detected");
        }
        /*if (COMMAND_INJECTION_PATTERN.matcher(value).find()) {
            isMalicious = true;
            logger.info("Command Injection detected");
        }*/
        if (FILE_INCLUSION_PATTERN.matcher(value).find()) {
            isMalicious = true;
        }
        if (HTML_INJECTION_PATTERN.matcher(value).find()) {
            isMalicious = true;
        }

        return isMalicious;
    }



}
