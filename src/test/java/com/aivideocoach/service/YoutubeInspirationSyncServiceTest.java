package com.aivideocoach.service;

import com.aivideocoach.youtube.dto.YoutubeInspirationRequest;
import com.aivideocoach.youtube.dto.YoutubeInspirationResponse;
import com.aivideocoach.youtube.dto.YoutubeVideoResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YoutubeInspirationSyncServiceTest {

    @Mock
    private YoutubeInspirationService youtubeService;

    @InjectMocks
    private YoutubeInspirationSyncService syncService;

    @Test
    void testWindowDaysPreservedInResponse() {
        // Given
        int requestWindowDays = 365;
        YoutubeInspirationRequest request = new YoutubeInspirationRequest(
                "TestBiz", "fitness", "men over 30", "US", "en", 
                List.of("workout"), requestWindowDays
        );

        YoutubeVideoResult mockVideo = new YoutubeVideoResult(
                "test123", "Test Video", "Test Channel", 
                OffsetDateTime.now().toString(), "https://youtube.com/watch?v=test123",
                10000L, 500L, 100L, 300, 50000L, 8.5
        );

        YoutubeInspirationResponse mockResponse = new YoutubeInspirationResponse(
                requestWindowDays, 1, List.of(mockVideo)
        );

        when(youtubeService.topVideos(any())).thenReturn(mockResponse);

        // When
        YoutubeInspirationResponse result = syncService.topVideos(request);

        // Then
        assertNotNull(result);
        assertEquals(requestWindowDays, result.windowDays(), "WindowDays should match request");
        assertEquals(1, result.totalCandidates());
        assertEquals(1, result.results().size());
    }

    @Test
    void testConcurrentRequestsWithDifferentWindowDays() throws Exception {
        // Given
        YoutubeInspirationRequest request180 = new YoutubeInspirationRequest(
                "TestBiz", "fitness", "men over 30", "US", "en", 
                List.of("workout"), 180
        );
        
        YoutubeInspirationRequest request730 = new YoutubeInspirationRequest(
                "TestBiz", "fitness", "men over 30", "US", "en", 
                List.of("workout"), 730
        );

        YoutubeVideoResult mockVideo = new YoutubeVideoResult(
                "test123", "Test Video", "Test Channel", 
                OffsetDateTime.now().toString(), "https://youtube.com/watch?v=test123",
                10000L, 500L, 100L, 300, 50000L, 8.5
        );

        when(youtubeService.topVideos(any())).thenAnswer(invocation -> {
            YoutubeInspirationRequest req = invocation.getArgument(0);
            return new YoutubeInspirationResponse(
                    req.windowDays(), 1, List.of(mockVideo)
            );
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // When - Execute concurrently
        CompletableFuture<YoutubeInspirationResponse> future180 = CompletableFuture
                .supplyAsync(() -> syncService.topVideos(request180), executor);
        
        CompletableFuture<YoutubeInspirationResponse> future730 = CompletableFuture
                .supplyAsync(() -> syncService.topVideos(request730), executor);

        YoutubeInspirationResponse result180 = future180.get();
        YoutubeInspirationResponse result730 = future730.get();

        // Then
        assertEquals(180, result180.windowDays(), "First request should have windowDays=180");
        assertEquals(730, result730.windowDays(), "Second request should have windowDays=730");
        
        executor.shutdown();
    }

    @Test
    void testFallbackResponsePreservesWindowDays() {
        // Given
        int requestWindowDays = 365;
        YoutubeInspirationRequest request = new YoutubeInspirationRequest(
                "TestBiz", "fitness", "men over 30", "US", "en", 
                List.of("workout"), requestWindowDays
        );

        // Mock error scenario - service returns error response
        YoutubeInspirationResponse errorResponse = new YoutubeInspirationResponse(
                requestWindowDays, 0, List.of()
        );
        
        when(youtubeService.topVideos(any())).thenReturn(errorResponse);

        // When
        YoutubeInspirationResponse result = syncService.topVideos(request);

        // Then
        assertNotNull(result);
        assertEquals(requestWindowDays, result.windowDays(), "Fallback should preserve windowDays");
        assertEquals(0, result.totalCandidates());
        assertEquals(0, result.results().size());
    }
}