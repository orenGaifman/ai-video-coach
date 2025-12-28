package com.aivideocoach.youtube.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YoutubeVideosResponse(List<Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String id,
            Snippet snippet,
            Statistics statistics,
            ContentDetails contentDetails
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snippet(
            String title,
            String channelTitle,
            String channelId,
            String publishedAt,
            Thumbnails thumbnails
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Thumbnails(Thumb high, Thumb medium, Thumb defaultThumb) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Thumb(String url) {}
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Statistics(String viewCount, String likeCount, String commentCount) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentDetails(String duration) {}
}
