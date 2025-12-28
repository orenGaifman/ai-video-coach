package com.aivideocoach.youtube;

public record YouTubeVideoDetails(
        String videoId,
        String title,
        String description,
        String channelTitle,
        String duration
) {}
