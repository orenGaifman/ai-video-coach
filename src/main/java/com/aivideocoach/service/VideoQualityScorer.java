package com.aivideocoach.service;

import com.aivideocoach.youtube.dto.YoutubeVideoResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class VideoQualityScorer {

    private static final Logger log = LoggerFactory.getLogger(VideoQualityScorer.class);

    private final Set<String> spamPatterns;
    private final long minViewCountThreshold;
    private final int recentVideoThresholdDays;

    public VideoQualityScorer(
            @Value("${inspiration.spam-patterns:free money,crypto pump,giveaway,click here,subscribe for,100% guaranteed}") String spamPatternsStr,
            @Value("${inspiration.min-view-count:10000}") long minViewCountThreshold,
            @Value("${inspiration.recent-video-threshold-days:7}") int recentVideoThresholdDays
    ) {
        this.spamPatterns = Set.of(spamPatternsStr.toLowerCase().split(","));
        this.minViewCountThreshold = minViewCountThreshold;
        this.recentVideoThresholdDays = recentVideoThresholdDays;
    }

    public List<YoutubeVideoResult> scoreAndRankVideos(List<YoutubeVideoResult> videos, String domain) {
        if (videos.isEmpty()) {
            return videos;
        }

        log.info("Scoring {} videos for quality ranking", videos.size());

        // Filter out obvious low-quality videos
        List<YoutubeVideoResult> filtered = videos.stream()
                .filter(video -> passesQualityFilters(video, domain))
                .collect(Collectors.toList());

        log.info("After quality filtering: {} videos remain from {} original", filtered.size(), videos.size());

        if (filtered.isEmpty()) {
            return filtered;
        }

        // Calculate scores and normalize
        List<ScoredVideo> scoredVideos = filtered.stream()
                .map(this::calculateRawScores)
                .collect(Collectors.toList());

        // Normalize scores within the candidate set
        normalizeScores(scoredVideos);

        // Sort by final score and convert back
        List<YoutubeVideoResult> ranked = scoredVideos.stream()
                .sorted((a, b) -> Double.compare(b.finalScore, a.finalScore))
                .map(sv -> new YoutubeVideoResult(
                        sv.video.videoId(),
                        sv.video.title(),
                        sv.video.channelTitle(),
                        sv.video.publishedAt(),
                        sv.video.url(),
                        sv.video.viewCount(),
                        sv.video.likeCount(),
                        sv.video.commentCount(),
                        sv.video.durationSeconds(),
                        sv.video.subscriberCount(),
                        sv.finalScore
                ))
                .collect(Collectors.toList());

        // Log top 5 for debugging
        logTopVideos(ranked);

        return ranked;
    }

    private boolean passesQualityFilters(YoutubeVideoResult video, String domain) {
        long viewCount = video.viewCount() != null ? video.viewCount() : 0;
        long likeCount = video.likeCount() != null ? video.likeCount() : 0;
        long daysSincePublish = calculateDaysSincePublish(video.publishedAt());

        // Filter 1: Minimum view count (unless very recent with high engagement)
        if (viewCount < minViewCountThreshold) {
            if (daysSincePublish > recentVideoThresholdDays) {
                return false;
            }
            // For recent videos, check engagement rate
            double engagementRate = calculateEngagementRate(video);
            if (engagementRate < 0.01) { // Less than 1% engagement
                return false;
            }
        }

        // Filter 2: Old videos with very low engagement
        if (viewCount > 0 && likeCount < 10 && daysSincePublish > 14) {
            return false;
        }

        // Filter 3: Spam title detection (unless domain-related)
        if (containsSpamPatterns(video.title(), domain)) {
            return false;
        }

        return true;
    }

    private boolean containsSpamPatterns(String title, String domain) {
        if (title == null) return false;
        
        String lowerTitle = title.toLowerCase();
        String lowerDomain = domain != null ? domain.toLowerCase() : "";

        for (String pattern : spamPatterns) {
            if (lowerTitle.contains(pattern.trim())) {
                // Allow if domain is related to the pattern
                if (lowerDomain.contains("crypto") && pattern.contains("crypto")) {
                    continue;
                }
                if (lowerDomain.contains("giveaway") && pattern.contains("giveaway")) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private ScoredVideo calculateRawScores(YoutubeVideoResult video) {
        long daysSincePublish = Math.max(1, calculateDaysSincePublish(video.publishedAt()));
        long viewCount = video.viewCount() != null ? video.viewCount() : 0;
        
        double velocity = viewCount / (double) daysSincePublish;
        double engagementRate = calculateEngagementRate(video);
        double channelBoost = Math.log10((video.subscriberCount() != null ? video.subscriberCount() : 0) + 10);
        double durationPenalty = calculateDurationPenalty(video.durationSeconds());

        return new ScoredVideo(video, velocity, engagementRate, viewCount, channelBoost, durationPenalty, 0.0);
    }

    private double calculateEngagementRate(YoutubeVideoResult video) {
        long viewCount = Math.max(1, video.viewCount() != null ? video.viewCount() : 1);
        long likeCount = video.likeCount() != null ? video.likeCount() : 0;
        long commentCount = video.commentCount() != null ? video.commentCount() : 0;
        
        return (likeCount + 2.0 * commentCount) / viewCount;
    }

    private double calculateDurationPenalty(Integer durationSeconds) {
        if (durationSeconds == null) return 0.0;
        
        // Prefer 2-20 minutes for long-form content
        if (durationSeconds >= 120 && durationSeconds <= 1200) { // 2-20 minutes
            return 0.0;
        } else if (durationSeconds < 60) { // Shorts penalty (small)
            return 0.05;
        } else if (durationSeconds > 1200) { // Very long videos
            return 0.1;
        }
        return 0.02; // Slight penalty for other durations
    }

    private long calculateDaysSincePublish(String publishedAt) {
        if (publishedAt == null) return 30; // Default to 30 days if unknown
        
        try {
            OffsetDateTime published = OffsetDateTime.parse(publishedAt);
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            return ChronoUnit.DAYS.between(published, now);
        } catch (Exception e) {
            log.warn("Failed to parse publishedAt: {}", publishedAt);
            return 30;
        }
    }

    private void normalizeScores(List<ScoredVideo> scoredVideos) {
        if (scoredVideos.size() <= 1) {
            scoredVideos.forEach(sv -> sv.finalScore = 1.0);
            return;
        }

        // Find min/max for each metric
        double minVelocity = scoredVideos.stream().mapToDouble(sv -> sv.velocity).min().orElse(0);
        double maxVelocity = scoredVideos.stream().mapToDouble(sv -> sv.velocity).max().orElse(1);
        double minEngagement = scoredVideos.stream().mapToDouble(sv -> sv.engagementRate).min().orElse(0);
        double maxEngagement = scoredVideos.stream().mapToDouble(sv -> sv.engagementRate).max().orElse(1);
        double minViews = scoredVideos.stream().mapToDouble(sv -> sv.viewCount).min().orElse(0);
        double maxViews = scoredVideos.stream().mapToDouble(sv -> sv.viewCount).max().orElse(1);
        double minChannel = scoredVideos.stream().mapToDouble(sv -> sv.channelBoost).min().orElse(0);
        double maxChannel = scoredVideos.stream().mapToDouble(sv -> sv.channelBoost).max().orElse(1);

        // Calculate final scores
        for (ScoredVideo sv : scoredVideos) {
            double normVelocity = normalize(sv.velocity, minVelocity, maxVelocity);
            double normEngagement = normalize(sv.engagementRate, minEngagement, maxEngagement);
            double normViews = normalize(sv.viewCount, minViews, maxViews);
            double normChannel = normalize(sv.channelBoost, minChannel, maxChannel);

            sv.finalScore = 0.55 * normVelocity + 
                           0.30 * normEngagement + 
                           0.10 * normViews + 
                           0.05 * normChannel - 
                           sv.durationPenalty;
        }
    }

    private double normalize(double value, double min, double max) {
        if (max == min) return 1.0;
        return (value - min) / (max - min);
    }

    private void logTopVideos(List<YoutubeVideoResult> ranked) {
        int topCount = Math.min(5, ranked.size());
        log.info("Top {} videos after quality scoring:", topCount);
        
        for (int i = 0; i < topCount; i++) {
            YoutubeVideoResult video = ranked.get(i);
            long daysSincePublish = calculateDaysSincePublish(video.publishedAt());
            double velocity = video.viewCount() != null ? video.viewCount() / Math.max(1.0, daysSincePublish) : 0;
            double engagementRate = calculateEngagementRate(video);
            
            log.info("#{}: '{}' by {} - Views: {}, Likes: {}, Comments: {}, Days: {}, Velocity: {:.1f}, Engagement: {:.4f}, Score: {:.3f}",
                    i + 1,
                    video.title(),
                    video.channelTitle(),
                    video.viewCount(),
                    video.likeCount(),
                    video.commentCount(),
                    daysSincePublish,
                    velocity,
                    engagementRate,
                    video.score()
            );
        }
    }

    private static class ScoredVideo {
        final YoutubeVideoResult video;
        final double velocity;
        final double engagementRate;
        final double viewCount;
        final double channelBoost;
        final double durationPenalty;
        double finalScore;

        ScoredVideo(YoutubeVideoResult video, double velocity, double engagementRate, 
                   double viewCount, double channelBoost, double durationPenalty, double finalScore) {
            this.video = video;
            this.velocity = velocity;
            this.engagementRate = engagementRate;
            this.viewCount = viewCount;
            this.channelBoost = channelBoost;
            this.durationPenalty = durationPenalty;
            this.finalScore = finalScore;
        }
    }
}