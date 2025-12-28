package com.aivideocoach.agent;

import com.aivideocoach.agent.memory.SessionStateStore;
import com.aivideocoach.agent.tools.InspirationTool;
import com.aivideocoach.agent.tools.WebsiteContextTool;
import com.aivideocoach.service.YoutubeInspirationSyncService;
import com.aivideocoach.service.WebsiteContextExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Bean
    public SessionStateStore sessionStateStore() {
        return new SessionStateStore();
    }

    @Bean
    public InspirationTool inspirationTool(YoutubeInspirationSyncService youtube) {
        return new InspirationTool(youtube);
    }

    @Bean
    public WebsiteContextTool websiteContextTool(WebsiteContextExtractor extractor) {
        return new WebsiteContextTool(extractor);
    }
}
