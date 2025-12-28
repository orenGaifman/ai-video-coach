package com.aivideocoach.youtube;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class YouTubeClient {

    private final WebClient webClient;
    private final String apiKey;

    public YouTubeClient(WebClient.Builder builder,
                         @Value("${youtube.api-key:}") String apiKeyFromYaml,
                         @Value("${YOUTUBE_API_KEY:}") String apiKeyFromEnv) {

        this.apiKey = (apiKeyFromEnv != null && !apiKeyFromEnv.isBlank()) ? apiKeyFromEnv : apiKeyFromYaml;

        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalArgumentException("YouTube API key must be defined (env YOUTUBE_API_KEY or youtube.api-key).");
        }

        this.webClient = builder.baseUrl("https://www.googleapis.com").build();
    }


    public Mono<YouTubeVideoDetails> getVideoDetails(String videoId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/youtube/v3/videos")
                        .queryParam("part", "snippet,contentDetails,statistics")
                        .queryParam("id", videoId)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(root -> {
                    JsonNode item = root.path("items").path(0);
                    if (item.isMissingNode() || item.isNull()) {
                        return Mono.error(new IllegalArgumentException("Video not found: " + videoId));
                    }

                    JsonNode snippet = item.path("snippet");
                    JsonNode titleNode = snippet.path("title");
                    JsonNode descriptionNode = snippet.path("description");
                    JsonNode channelTitleNode = snippet.path("channelTitle");
                    JsonNode durationNode = item.path("contentDetails").path("duration");
                    
                    String title = titleNode.isMissingNode() ? null : titleNode.asText();
                    String description = descriptionNode.isMissingNode() ? null : descriptionNode.asText();
                    String channelTitle = channelTitleNode.isMissingNode() ? null : channelTitleNode.asText();
                    String duration = durationNode.isMissingNode() ? null : durationNode.asText();

                    return Mono.just(new YouTubeVideoDetails(videoId, title, description, channelTitle, duration));
                });
    }

}
