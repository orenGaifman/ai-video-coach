package com.aivideocoach.service;


import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class VideoAnalysisService {

    private final VideoContextService videoContextService;
    private final VideoCoachService agentService;

    public Mono<VideoAnalysisResult> analyze(String videoId) {
        return videoContextService.buildContextFromYouTubeVideoId(videoId)
                .flatMap(ctx -> agentService.askCoach(ctx))
                .map(raw -> normalize(raw, videoId));
    }

    private VideoAnalysisResult normalize(String raw, String videoId) {
        return new VideoAnalysisResult(videoId, raw);
    }

    public record VideoAnalysisResult(String videoId, String analysisText) {}
}
