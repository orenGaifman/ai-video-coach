package com.aivideocoach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "anthropic")
public class AnthropicProperties {
    /**
     * Optional toggle for tests/local runs
     */
    private boolean enabled = true;

    private String apiKey;
    private String model = "claude-3-5-haiku-latest";
    private Double temperature = 0.2;

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public Double getTemperature() {
        return temperature;
    }
}
