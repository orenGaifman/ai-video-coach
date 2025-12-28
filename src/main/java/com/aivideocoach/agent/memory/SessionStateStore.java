package com.aivideocoach.agent.memory;

import com.aivideocoach.agent.dto.AgentState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStateStore {

    private final Map<String, AgentState> sessions = new ConcurrentHashMap<>();

    public AgentState getOrCreate(String sessionId) {
        return sessions.computeIfAbsent(sessionId, k -> AgentState.empty());
    }

    public void put(String sessionId, AgentState state) {
        sessions.put(sessionId, state);
    }

    public void clear(String sessionId) {
        sessions.remove(sessionId);
    }
}
