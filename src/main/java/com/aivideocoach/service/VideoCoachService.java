package com.aivideocoach.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class VideoCoachService {

    private final ChatLanguageModel chatModel;

    public Mono<String> askCoach(VideoContextService.VideoContext ctx) {
        String prompt = buildPrompt(ctx);
        return Mono.fromSupplier(() -> chatModel.generate(prompt));
    }

    public Mono<String> askCoach(String prompt) {
        return Mono.fromSupplier(() -> chatModel.generate(prompt));
    }

    private String buildPrompt(VideoContextService.VideoContext ctx) {
        return """
        You are a professional video coach.

        The user is a content creator who wants to recreate a successful video
        in their own style.

        Analyze the following YouTube video metadata and provide:

        1. Why this video works
        2. Key hook in the first 3–5 seconds
        3. Structure of the video (beats / flow)
        4. How to recreate it in a different personal style
        5. Filming tips (camera, lighting, framing)
        6. Common mistakes to avoid

        Video title:
        %s

        Video description:
        %s
        """
                .formatted(
                        safe(ctx.title()),
                        safe(ctx.description())
                );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
