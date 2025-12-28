package com.aivideocoach.youtube.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YoutubeChannelsResponse(List<Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String id,
            Statistics statistics
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Statistics(String subscriberCount, String videoCount, String viewCount) {}
}