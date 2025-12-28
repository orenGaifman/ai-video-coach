package com.aivideocoach.controller;

import com.aivideocoach.agent.llm.InspirationChatAgent;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final InspirationChatAgent agent;

    public AgentController(InspirationChatAgent agent) {
        this.agent = agent;
    }

    public record ChatRequest(String sessionId, String message) {}
    public record ChatResponse(String assistantMessage) {}

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest req) {
        String sessionId = (req.sessionId == null || req.sessionId.isBlank()) 
                ? "default" 
                : req.sessionId;
        String answer = agent.chat(sessionId, req.message);
        return new ChatResponse(answer);
    }
}
