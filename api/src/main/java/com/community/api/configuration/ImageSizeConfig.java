package com.community.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "image.size")
@Getter
@Setter
public class ImageSizeConfig {

    private String min;
    private String max;

    public long getMinInBytes() {
        return convertToBytes(min);
    }

    public long getMaxInBytes() {
        return convertToBytes(max);
    }

    private long convertToBytes(String size) {
        if (size.endsWith("MB")) {
            return Long.parseLong(size.replace("MB", "")) * 1024 * 1024;
        } else if (size.endsWith("KB")) {
            return Long.parseLong(size.replace("KB", "")) * 1024;
        }
        return Long.parseLong(size);
    }

}
