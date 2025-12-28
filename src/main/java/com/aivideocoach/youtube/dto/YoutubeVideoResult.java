package com.aivideocoach.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YoutubeVideoResult(
        String videoId,
        String title,
        String channelTitle,
        String publishedAt,
        String url,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        Integer durationSeconds,
        Long subscriberCount,
        Double score
) {
    public String getThumbnailUrl() {
        return videoId != null ? "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg" : null;
    }
    
    public String getVideoUrl() {
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalStateException("Video ID is missing - cannot generate YouTube URL");
        }
        return "https://www.youtube.com/watch?v=" + videoId;
    }
    
    // Validation method to ensure URL is always available
    public boolean hasValidUrl() {
        return videoId != null && !videoId.isBlank() && url != null && !url.isBlank();
    }
}

