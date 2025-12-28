package com.aivideocoach.controller;

import com.aivideocoach.config.YouTubeProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("/api/test")
public class YouTubeTestController {

    private final YouTubeProperties props;
    private final HttpClient httpClient;

    public YouTubeTestController(YouTubeProperties props) {
        this.props = props;
        this.httpClient = HttpClient.newHttpClient();
    }

    @GetMapping("/youtube-key")
    public String testYouTubeKey() {
        try {
            String testUrl = props.getBaseUrl() + "/search?part=snippet&type=video&maxResults=1&q=test&key=" + props.getApiKey();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(testUrl))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                return "YouTube API Key is VALID. Response: " + body.substring(0, Math.min(200, body.length()));
            } else {
                return "YouTube API Key is INVALID. Status: " + response.statusCode() + ", Body: " + response.body();
            }
        } catch (Exception e) {
            return "YouTube API Key test FAILED: " + e.getMessage();
        }
    }
}