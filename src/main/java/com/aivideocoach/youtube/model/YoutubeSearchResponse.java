package com.aivideocoach.youtube.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YoutubeSearchResponse(List<Item> items, PageInfo pageInfo) {
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(Id id, Snippet snippet) {
        
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Id(String videoId) {}
        
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Snippet(String title, String channelTitle, String publishedAt) {}
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PageInfo(Integer totalResults, Integer resultsPerPage) {}
}