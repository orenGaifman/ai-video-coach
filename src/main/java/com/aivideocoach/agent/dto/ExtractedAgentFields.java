package com.aivideocoach.agent.dto;

import java.util.List;

public record ExtractedAgentFields(
        String businessName,
        String domain,
        String targetAudience,
        List<String> keywords,
        String language,
        String location
) {}
