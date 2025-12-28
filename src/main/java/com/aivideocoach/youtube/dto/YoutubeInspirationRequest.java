package com.aivideocoach.youtube.dto;


import java.util.List;

public record YoutubeInspirationRequest(
        String businessName,
        String domain,
        String targetAudience,
        String location,      // e.g. "IL"
        String language,      // e.g. "he" or "en"
        List<String> keywords, // optional
        Integer windowDays    // optional, defaults to 90
) {}
