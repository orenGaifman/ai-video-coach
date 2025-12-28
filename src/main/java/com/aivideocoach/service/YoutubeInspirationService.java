package com.aivideocoach.service;

import com.aivideocoach.config.YouTubeProperties;
import com.aivideocoach.youtube.dto.YoutubeInspirationRequest;
import com.aivideocoach.youtube.dto.YoutubeInspirationResponse;
import com.aivideocoach.youtube.dto.YoutubeVideoResult;
import com.aivideocoach.youtube.model.YoutubeSearchResponse;
import com.aivideocoach.youtube.model.YoutubeVideosResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class YoutubeInspirationService {

    private static final Logger log = LoggerFactory.getLogger(YoutubeInspirationService.class);
    
    private static final int WINDOW_DAYS = 30;
    private static final int SEARCH_MAX_RESULTS = 25;
    private static final int TOP_N = 10;

    private final YouTubeProperties props;
    private final VideoQualityScorer qualityScorer;
    private final YouTubeDurationParser durationParser;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YoutubeInspirationService(YouTubeProperties props,
                                     VideoQualityScorer qualityScorer,
                                     YouTubeDurationParser durationParser) {
        this.props = props;
        this.qualityScorer = qualityScorer;
        this.durationParser = durationParser;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public YoutubeInspirationResponse topVideos(YoutubeInspirationRequest req) {
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[{}] SERVICE_START: domain='{}', windowDays={}", correlationId, req.domain(), req.windowDays());
            
            if (props.getApiKey() == null || props.getApiKey().isBlank()) {
                log.error("[{}] NO_API_KEY", correlationId);
                return createErrorResponse(req.windowDays(), "YouTube API key not configured");
            }
            
            // Build simple query from domain and first keyword
            String query = buildSimpleQuery(req);
            log.info("[{}] QUERY: '{}'", correlationId, query);
            
            // Get published after date
            int windowDays = req.windowDays() != null ? req.windowDays() : WINDOW_DAYS;
            String publishedAfter = OffsetDateTime.now(ZoneOffset.UTC)
                    .minusDays(windowDays)
                    .toString();
            
            // Search for videos
            List<String> videoIds = searchVideos(correlationId, query, publishedAfter, req);
            log.info("[{}] VIDEO_IDS_FOUND: count={}", correlationId, videoIds.size());
            
            if (videoIds.isEmpty()) {
                return createErrorResponse(windowDays, "No videos found");
            }
            
            // Get video details
            List<YoutubeVideoResult> videos = getVideoDetails(correlationId, videoIds);
            log.info("[{}] VIDEO_DETAILS_FETCHED: count={}", correlationId, videos.size());
            
            // Score and rank videos
            List<YoutubeVideoResult> rankedVideos = qualityScorer.scoreAndRankVideos(videos, req.domain());
            List<YoutubeVideoResult> topResults = rankedVideos.stream()
                    .filter(video -> video.hasValidUrl()) // Ensure all videos have valid URLs
                    .limit(TOP_N)
                    .collect(Collectors.toList());
            
            // Log warning if any videos were filtered out due to missing URLs
            int filteredCount = rankedVideos.size() - topResults.size();
            if (filteredCount > 0) {
                log.warn("[{}] FILTERED_VIDEOS: {} videos removed due to missing URLs", correlationId, filteredCount);
            }
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[{}] SERVICE_COMPLETED: results={}, elapsed={}ms", correlationId, topResults.size(), elapsed);
            
            return new YoutubeInspirationResponse(windowDays, videoIds.size(), topResults);
            
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[{}] SERVICE_ERROR: elapsed={}ms, error={}", correlationId, elapsed, e.getMessage(), e);
            return createErrorResponse(req.windowDays(), "Service error: " + e.getMessage());
        }
    }

    private String buildSimpleQuery(YoutubeInspirationRequest req) {
        StringBuilder query = new StringBuilder();
        
        if (req.domain() != null && !req.domain().isBlank()) {
            query.append(req.domain());
        }
        
        if (req.keywords() != null && !req.keywords().isEmpty()) {
            String firstKeyword = req.keywords().get(0);
            if (firstKeyword != null && !firstKeyword.isBlank()) {
                if (query.length() > 0) query.append(" ");
                query.append(firstKeyword);
            }
        }
        
        return query.length() > 0 ? query.toString() : "business";
    }

    private List<String> searchVideos(String correlationId, String query, String publishedAfter, YoutubeInspirationRequest req) throws Exception {
        // Build URL
        StringBuilder url = new StringBuilder(props.getBaseUrl() + "/search");
        url.append("?part=snippet");
        url.append("&type=video");
        url.append("&order=viewCount");
        url.append("&maxResults=").append(SEARCH_MAX_RESULTS);
        url.append("&q=").append(URLEncoder.encode(query, StandardCharsets.UTF_8));
        url.append("&publishedAfter=").append(URLEncoder.encode(publishedAfter, StandardCharsets.UTF_8));
        url.append("&key=").append(props.getApiKey());
        
        if (req.language() != null && !req.language().isBlank()) {
            url.append("&relevanceLanguage=").append(req.language());
        }
        if (req.location() != null && !req.location().isBlank()) {
            url.append("&regionCode=").append(req.location());
        }
        
        String maskedUrl = url.toString().replaceAll("key=[^&]+", "key=***");
        log.info("[{}] SEARCH_URL: {}", correlationId, maskedUrl);
        
        // Make HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        log.info("[{}] SEARCH_RESPONSE: status={}", correlationId, response.statusCode());
        
        if (response.statusCode() != 200) {
            log.error("[{}] SEARCH_ERROR: status={}, body={}", correlationId, response.statusCode(), response.body());
            throw new RuntimeException("YouTube API error: " + response.statusCode());
        }
        
        // Parse response
        YoutubeSearchResponse searchResponse = objectMapper.readValue(response.body(), YoutubeSearchResponse.class);
        
        List<String> videoIds = new ArrayList<>();
        if (searchResponse.items() != null) {
            for (var item : searchResponse.items()) {
                if (item.id() != null && item.id().videoId() != null) {
                    videoIds.add(item.id().videoId());
                }
            }
        }
        
        return videoIds;
    }

    private List<YoutubeVideoResult> getVideoDetails(String correlationId, List<String> videoIds) throws Exception {
        if (videoIds.isEmpty()) {
            return List.of();
        }
        
        // Build URL
        String idsParam = String.join(",", videoIds);
        String url = props.getBaseUrl() + "/videos" +
                "?part=snippet,statistics,contentDetails" +
                "&id=" + URLEncoder.encode(idsParam, StandardCharsets.UTF_8) +
                "&key=" + props.getApiKey();
        
        String maskedUrl = url.replaceAll("key=[^&]+", "key=***");
        log.info("[{}] VIDEOS_URL: {}", correlationId, maskedUrl);
        
        // Make HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        log.info("[{}] VIDEOS_RESPONSE: status={}", correlationId, response.statusCode());
        
        if (response.statusCode() != 200) {
            log.error("[{}] VIDEOS_ERROR: status={}, body={}", correlationId, response.statusCode(), response.body());
            return List.of();
        }
        
        // Parse response
        YoutubeVideosResponse videosResponse = objectMapper.readValue(response.body(), YoutubeVideosResponse.class);
        
        List<YoutubeVideoResult> results = new ArrayList<>();
        if (videosResponse.items() != null) {
            for (var item : videosResponse.items()) {
                YoutubeVideoResult video = convertToVideoResult(item);
                if (video != null) {
                    results.add(video);
                }
            }
        }
        
        return results;
    }

    private YoutubeVideoResult convertToVideoResult(YoutubeVideosResponse.Item item) {
        if (item == null || item.id() == null) return null;
        
        String title = item.snippet() != null ? item.snippet().title() : "";
        String channelTitle = item.snippet() != null ? item.snippet().channelTitle() : "";
        String publishedAt = item.snippet() != null ? item.snippet().publishedAt() : "";
        String url = "https://www.youtube.com/watch?v=" + item.id();
        
        Long viewCount = parseLongSafe(item.statistics() != null ? item.statistics().viewCount() : null);
        Long likeCount = parseLongSafe(item.statistics() != null ? item.statistics().likeCount() : null);
        Long commentCount = parseLongSafe(item.statistics() != null ? item.statistics().commentCount() : null);
        
        Integer durationSeconds = 0;
        if (item.contentDetails() != null && item.contentDetails().duration() != null) {
            durationSeconds = durationParser.parseToSeconds(item.contentDetails().duration());
        }
        
        return new YoutubeVideoResult(
                item.id(), title, channelTitle, publishedAt, url,
                viewCount != null ? viewCount : 0L,
                likeCount != null ? likeCount : 0L,
                commentCount != null ? commentCount : 0L,
                durationSeconds != null ? durationSeconds : 0,
                0L, 0.0
        );
    }

    private static Long parseLongSafe(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Long.parseLong(s); } catch (Exception e) { return null; }
    }

    private YoutubeInspirationResponse createErrorResponse(Integer windowDays, String reason) {
        int actualWindowDays = windowDays != null ? windowDays : WINDOW_DAYS;
        return new YoutubeInspirationResponse(
                actualWindowDays,
                0,
                List.of(new YoutubeVideoResult(
                        "error", reason, "System", OffsetDateTime.now().toString(), "#",
                        0L, 0L, 0L, 0, 0L, 0.0
                ))
        );
    }
}