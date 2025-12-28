package com.aivideocoach.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "youtube")
public class YouTubeProperties {
    private String baseUrl = "https://www.googleapis.com/youtube/v3";
    private String apiKey;
    private boolean debug = false;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }
}
