package com.aivideocoach.service;

import com.aivideocoach.youtube.dto.YoutubeVideoResult;

/**
 * Internal data structure to hold video information with channel ID during processing
 */
public record EnrichedVideoData(
        String videoId,
        String title,
        String channelTitle,
        String channelId,
        String publishedAt,
        String url,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        Integer durationSeconds,
        Long subscriberCount
) {
    
    public YoutubeVideoResult toVideoResult(Double score) {
        return new YoutubeVideoResult(
                videoId, title, channelTitle, publishedAt, url,
                viewCount, likeCount, commentCount, durationSeconds,
                subscriberCount, score
        );
    }
}