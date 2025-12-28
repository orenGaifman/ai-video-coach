package com.aivideocoach.controller;

import com.aivideocoach.youtube.dto.YoutubeInspirationRequest;
import com.aivideocoach.youtube.dto.YoutubeInspirationResponse;
import com.aivideocoach.service.YoutubeInspirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inspiration")
public class InspirationController {

    private static final Logger log = LoggerFactory.getLogger(InspirationController.class);
    
    private final YoutubeInspirationService service;

    public InspirationController(YoutubeInspirationService service) {
        this.service = service;
    }

    @PostMapping("/top-youtube")
    public YoutubeInspirationResponse topYoutube(@RequestBody YoutubeInspirationRequest req) {
        log.info("Received inspiration request: domain={}, targetAudience={}, keywords={}, language={}, location={}", 
                req.domain(), req.targetAudience(), 
                req.keywords() != null ? req.keywords().size() : 0, 
                req.language(), req.location());
        
        YoutubeInspirationResponse response = service.topVideos(req);
        
        log.info("Returning inspiration response: windowDays={}, totalCandidates={}, results={}", 
                response.windowDays(), response.totalCandidates(), 
                response.results() != null ? response.results().size() : 0);
        
        return response;
    }
}