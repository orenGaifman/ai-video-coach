package com.aivideocoach.agent.dto;

import java.util.List;

public record AgentState(
        String businessName,
        String domain,
        String targetAudience,
        List<String> keywords,
        String language,
        String location
) {

    public static AgentState empty() {
        return new AgentState(null, null, null, null, null, null);
    }

    public static AgentState merge(AgentState current, ExtractedAgentFields e) {
        if (current == null) current = empty();
        if (e == null) return current;

        return new AgentState(
                firstNonBlank(current.businessName(), e.businessName()),
                firstNonBlank(current.domain(), e.domain()),
                firstNonBlank(current.targetAudience(), e.targetAudience()),
                (current.keywords() != null && !current.keywords().isEmpty())
                        ? current.keywords()
                        : e.keywords(),
                firstNonBlank(current.language(), e.language()),
                firstNonBlank(current.location(), e.location())
        );
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
