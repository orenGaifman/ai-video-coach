package com.aivideocoach.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouTubeVideosResponseDto {
    private List<YouTubeVideoItemDto> items;
}
