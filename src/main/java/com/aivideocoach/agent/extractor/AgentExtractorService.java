package com.aivideocoach.agent.extractor;

import com.aivideocoach.agent.dto.ExtractedAgentFields;
import com.aivideocoach.agent.util.JsonUtils;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

@Service
public class AgentExtractorService {

    private final ChatLanguageModel model;

    public AgentExtractorService(ChatLanguageModel model) {
        this.model = model;
    }

    public ExtractedAgentFields extract(String userMessage) {

        String prompt = """
        You are an information extraction engine for a business video inspiration app.
        Extract fields from the user message and return ONLY valid JSON.
        If a field is unknown or not mentioned, set it to null.

        JSON schema:
        {
          "businessName": string|null,
          "domain": string|null,
          "targetAudience": string|null,
          "keywords": array<string>|null,
          "language": string|null,     // use ISO-639-1 like "he" or "en" if possible
          "location": string|null      // use country code like "IL" or "US" if possible
        }

        IMPORTANT:
        - Output MUST be JSON only (no markdown, no explanation)
        - keywords should be 3-8 short phrases if user provided them

        User message:
        "%s"
        """.formatted(userMessage == null ? "" : userMessage.trim());

        String json = model.generate(prompt);
        return JsonUtils.fromJson(json, ExtractedAgentFields.class);
    }
}
