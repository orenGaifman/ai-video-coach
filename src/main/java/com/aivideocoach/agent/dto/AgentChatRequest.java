package com.aivideocoach.agent.dto;

public record AgentChatRequest(
        String sessionId,
        String message
) {}
