package com.aivideocoach.service;

import com.aivideocoach.youtube.dto.YoutubeInspirationRequest;
import com.aivideocoach.youtube.dto.YoutubeInspirationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class YoutubeInspirationSyncService {

    private static final Logger log = LoggerFactory.getLogger(YoutubeInspirationSyncService.class);
    
    private final YoutubeInspirationService youtubeService;

    public YoutubeInspirationSyncService(YoutubeInspirationService youtubeService) {
        this.youtubeService = youtubeService;
    }

    public YoutubeInspirationResponse topVideos(YoutubeInspirationRequest request) {
        log.info("SYNC_START: domain={}, keywords={}, language={}, location={}, windowDays={}", 
                request.domain(), 
                request.keywords() != null ? request.keywords().size() : 0, 
                request.language(), 
                request.location(), 
                request.windowDays());
        
        // Direct call - no threading, no Mono, no complexity
        YoutubeInspirationResponse response = youtubeService.topVideos(request);
        
        log.info("SYNC_RESPONSE: windowDays={}, totalCandidates={}, results={}", 
                response != null ? response.windowDays() : null,
                response != null ? response.totalCandidates() : null,
                response != null && response.results() != null ? response.results().size() : 0);
        
        return response;
    }
}