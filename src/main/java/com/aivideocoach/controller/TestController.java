package com.aivideocoach.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final WebClient webClient;
    private final String youtubeApiKey;

    public TestController(@Value("${youtube.api-key:}") String youtubeApiKey) {
        this.youtubeApiKey = youtubeApiKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://www.googleapis.com/youtube/v3")
                .build();
    }

    @GetMapping("/youtube-api")
    public Mono<String> testYouTubeApi() {
        if (youtubeApiKey == null || youtubeApiKey.isEmpty()) {
            return Mono.just("❌ YouTube API key not configured. Set YOUTUBE_API_KEY environment variable.");
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("part", "snippet")
                        .queryParam("type", "video")
                        .queryParam("maxResults", "1")
                        .queryParam("q", "test")
                        .queryParam("key", youtubeApiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> "✅ YouTube API is working! Response length: " + response.length())
                .onErrorResume(error -> {
                    if (error.getMessage().contains("403")) {
                        return Mono.just("❌ YouTube API key is invalid or quota exceeded. Error: " + error.getMessage());
                    }
                    return Mono.just("❌ YouTube API error: " + error.getMessage());
                });
    }
}