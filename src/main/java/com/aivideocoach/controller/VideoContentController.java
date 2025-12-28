package com.aivideocoach.controller;

import com.aivideocoach.agent.llm.VideoContentAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video-content")
public class VideoContentController {

    private static final Logger log = LoggerFactory.getLogger(VideoContentController.class);
    
    private final VideoContentAgent videoContentAgent;

    public VideoContentController(VideoContentAgent videoContentAgent) {
        this.videoContentAgent = videoContentAgent;
    }

    @PostMapping("/analyze")
    public String analyzeVideo(@RequestBody VideoAnalysisRequest request) {
        log.info("Received video analysis request for session: {}", request.sessionId());
        
        return videoContentAgent.analyzeAndGuide(request.sessionId(), request.message());
    }
    
    public record VideoAnalysisRequest(String sessionId, String message) {}
}