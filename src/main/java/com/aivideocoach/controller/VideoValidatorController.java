package com.aivideocoach.controller;

import com.aivideocoach.agent.llm.VideoQualityValidatorAgent;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class VideoValidatorController {

    private final VideoQualityValidatorAgent validator;

    public VideoValidatorController(VideoQualityValidatorAgent validator) {
        this.validator = validator;
    }

    public record VideoValidationRequest(
            String businessSummary,
            String targetAudience,
            String videoTitle,
            String videoDescription,
            String channelName,
            Long viewCount,
            String publishDate
    ) {}

    @PostMapping("/validate")
    public String validateVideo(@RequestBody VideoValidationRequest req) {
        String evaluationRequest = String.format("""
            Please evaluate this YouTube video for business relevance:
            
            Business: %s
            Target Audience: %s
            
            Video Details:
            - Title: %s
            - Description: %s
            - Channel: %s
            - Views: %s
            - Published: %s
            """, 
            req.businessSummary, req.targetAudience, req.videoTitle, 
            req.videoDescription != null ? req.videoDescription.substring(0, Math.min(200, req.videoDescription.length())) : "N/A",
            req.channelName, req.viewCount, req.publishDate);

        return validator.validateVideo(evaluationRequest);
    }
}