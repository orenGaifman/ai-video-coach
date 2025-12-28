package com.aivideocoach.agent.dto;

import com.aivideocoach.youtube.dto.YoutubeVideoResult;

import java.util.List;

public record AgentChatResponse(
        String sessionId,
        String assistantMessage,
        AgentState state,
        List<String> missingFields,
        List<YoutubeVideoResult> videos
) {}
