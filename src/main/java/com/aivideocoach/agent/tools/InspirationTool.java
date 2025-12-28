package com.aivideocoach.agent.tools;

import com.aivideocoach.service.YoutubeInspirationSyncService;
import com.aivideocoach.youtube.dto.YoutubeInspirationRequest;
import com.aivideocoach.youtube.dto.YoutubeInspirationResponse;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InspirationTool {
    
    private final YoutubeInspirationSyncService service;
    
    public InspirationTool(YoutubeInspirationSyncService service) {
        this.service = service;
    }
    
    @Tool("Finds top YouTube videos with quality ranking")
    public YoutubeInspirationResponse topYoutube(
            String businessName,
            String domain,
            String targetAudience,
            List<String> keywords,
            String language,
            String location
    ) {
        YoutubeInspirationRequest req = new YoutubeInspirationRequest(
                businessName,
                domain,
                targetAudience,
                location,
                language != null ? language : "en",
                keywords,
                30  // Simple 30 day window
        );

        return service.topVideos(req);
    }
}