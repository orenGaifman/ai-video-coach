package com.aivideocoach.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YoutubeInspirationResponse(
        Integer windowDays,
        Integer totalCandidates,
        List<YoutubeVideoResult> results
) {}
