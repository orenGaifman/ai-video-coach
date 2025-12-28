package com.aivideocoach.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebsiteContext(
        String brandName,
        List<String> offers,
        String audience,
        List<String> topics,
        List<String> differentiators,
        List<String> keyPhrases,
        String languageGuess
) {}