package com.aivideocoach.agent.tools;

import com.aivideocoach.agent.llm.VideoQualityValidatorAgent;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class VideoQualityValidatorTool {

    private final VideoQualityValidatorAgent validatorAgent;

    public VideoQualityValidatorTool(VideoQualityValidatorAgent validatorAgent) {
        this.validatorAgent = validatorAgent;
    }

    @Tool("Validates YouTube video quality and relevance for business inspiration")
    public String validateVideo(
            String businessSummary,
            String targetAudience,
            String videoTitle,
            String videoDescription,
            String channelName,
            Long viewCount,
            String publishDate
    ) {
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
            businessSummary, targetAudience, videoTitle, 
            videoDescription != null ? videoDescription.substring(0, Math.min(200, videoDescription.length())) : "N/A",
            channelName, viewCount, publishDate);

        return validatorAgent.validateVideo(evaluationRequest);
    }
}