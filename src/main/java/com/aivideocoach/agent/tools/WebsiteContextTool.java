package com.aivideocoach.agent.tools;

import com.aivideocoach.agent.dto.WebsiteContext;
import com.aivideocoach.service.WebsiteContextExtractor;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class WebsiteContextTool {
    
    private final WebsiteContextExtractor extractor;
    
    public WebsiteContextTool(WebsiteContextExtractor extractor) {
        this.extractor = extractor;
    }
    
    @Tool("Extracts business context from a website URL to understand niche, audience, and key offerings")
    public WebsiteContext fetchWebsiteContext(String url) {
        return extractor.extractContext(url);
    }
}