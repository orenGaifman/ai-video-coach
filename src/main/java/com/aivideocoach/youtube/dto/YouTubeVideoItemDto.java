package com.aivideocoach.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouTubeVideoItemDto {

    private String id;
    private Snippet snippet;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snippet {
        private String title;
        private String description;
        private String channelTitle;
    }
}
