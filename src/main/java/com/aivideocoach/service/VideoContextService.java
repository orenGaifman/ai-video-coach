package com.aivideocoach.service;

import com.aivideocoach.youtube.YouTubeClient;
import com.aivideocoach.youtube.YouTubeVideoDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class VideoContextService {

    private final YouTubeClient youTubeClient;

    public Mono<VideoContext> buildContextFromYouTubeVideoId(String videoId) {
        return youTubeClient.getVideoDetails(videoId)
                .map(details -> new VideoContext(
                        details.videoId(),
                        details.title(),
                        details.description()
                ));
    }


    public record VideoContext(String videoId, String title, String description) {}
}
