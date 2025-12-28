package com.aivideocoach.service;

import com.aivideocoach.agent.dto.WebsiteContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WebsiteContextExtractor {

    private static final Logger log = LoggerFactory.getLogger(WebsiteContextExtractor.class);
    private static final Pattern HEBREW_PATTERN = Pattern.compile("[\\u0590-\\u05FF]");
    private static final Set<String> COMMON_PATHS = Set.of("/about", "/services", "/products", "/solutions");

    private WebsiteContextExtractor() {
        // No WebClient needed anymore - using Java HTTP client
    }

    public WebsiteContext extractContext(String url) {
        // Ensure URL has protocol
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        
        log.info("Extracting website context from: {}", url);
        
        try {
            // Fetch main page synchronously without blocking reactor threads
            String mainContent = fetchPageContentSync(url);
            
            // Try to fetch additional pages
            List<String> additionalContent = new ArrayList<>();
            String baseUrl = extractBaseUrl(url);
            
            for (String path : COMMON_PATHS) {
                try {
                    String additionalUrl = baseUrl + path;
                    String content = fetchPageContentSync(additionalUrl);
                    if (content != null && !content.isEmpty()) {
                        additionalContent.add(content);
                    }
                } catch (Exception e) {
                    // Ignore failures for additional pages
                }
            }

            // Combine all content
            String allContent = mainContent + " " + String.join(" ", additionalContent);
            
            return analyzeContent(allContent, url);
        } catch (Exception e) {
            log.warn("Failed to extract website context from {}: {}", url, e.getMessage());
            return createFallbackContext(url);
        }
    }

    private String fetchPageContentSync(String url) {
        try {
            // Use Java's built-in HTTP client instead of WebClient to avoid reactor issues
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .build();
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (compatible; VideoCoachBot/1.0)")
                    .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return extractTextFromHtml(response.body());
            } else {
                log.debug("HTTP {} for URL: {}", response.statusCode(), url);
                return "";
            }
        } catch (Exception e) {
            log.debug("Failed to fetch {}: {}", url, e.getMessage());
            return "";
        }
    }

    private String extractTextFromHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        
        try {
            Document doc = Jsoup.parse(html);
            
            // Remove unwanted elements
            doc.select("script, style, nav, footer, header, .menu, .navigation, .sidebar").remove();
            
            // Extract meaningful text
            Elements titleElements = doc.select("title, h1, h2, h3");
            Elements contentElements = doc.select("p, div, span, li");
            Elements metaElements = doc.select("meta[name=description], meta[property=og:description]");
            
            StringBuilder text = new StringBuilder();
            
            // Add title and headers (higher weight)
            titleElements.forEach(el -> {
                String elementText = el.text();
                if (elementText != null && !elementText.isEmpty()) {
                    text.append(elementText).append(" ");
                }
            });
            
            // Add meta descriptions
            metaElements.forEach(el -> {
                String content = el.attr("content");
                if (content != null && !content.isEmpty()) {
                    text.append(content).append(" ");
                }
            });
            
            // Add content (limited)
            String contentText = contentElements.stream()
                    .map(Element::text)
                    .filter(t -> t != null && t.length() > 10)
                    .limit(50)
                    .collect(Collectors.joining(" "));
            
            if (contentText != null) {
                text.append(contentText);
            }
            
            return text.toString().trim();
        } catch (Exception e) {
            log.warn("Failed to parse HTML: {}", e.getMessage());
            return "";
        }
    }

    private WebsiteContext analyzeContent(String content, String url) {
        if (content.isEmpty()) {
            return createFallbackContext(url);
        }

        // Simple keyword extraction and analysis
        String[] words = content.toLowerCase().split("\\s+");
        Map<String, Integer> wordFreq = new HashMap<>();
        
        for (String word : words) {
            if (word.length() > 3 && !isStopWord(word)) {
                wordFreq.merge(word, 1, Integer::sum);
            }
        }

        // Extract top keywords
        List<String> topKeywords = wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Detect language
        String language = HEBREW_PATTERN.matcher(content).find() ? "he" : "en";
        
        // Extract brand name (simple heuristic)
        String brandName = extractBrandName(content, url);
        
        // Categorize content
        List<String> offers = extractOffers(content, topKeywords);
        String audience = inferAudience(content);
        List<String> topics = extractTopics(topKeywords);
        List<String> differentiators = extractDifferentiators(content);
        
        // Limit total response size
        return new WebsiteContext(
                truncate(brandName, 50),
                truncateList(offers, 3, 40),
                truncate(audience, 80),
                truncateList(topics, 4, 30),
                truncateList(differentiators, 3, 40),
                truncateList(topKeywords, 6, 20),
                language
        );
    }

    private String extractBrandName(String content, String url) {
        try {
            String domain = URI.create(url).getHost();
            if (domain != null) {
                return domain.replaceAll("^www\\.", "").split("\\.")[0];
            }
        } catch (Exception e) {
            // Ignore
        }
        return "Unknown";
    }

    private List<String> extractOffers(String content, List<String> keywords) {
        List<String> offers = new ArrayList<>();
        String lowerContent = content.toLowerCase();
        
        // Look for service/product indicators
        if (lowerContent.contains("service") || lowerContent.contains("solution")) {
            offers.add("services");
        }
        if (lowerContent.contains("product") || lowerContent.contains("sell")) {
            offers.add("products");
        }
        if (lowerContent.contains("consult") || lowerContent.contains("advice")) {
            offers.add("consulting");
        }
        
        return offers.isEmpty() ? List.of("business solutions") : offers;
    }

    private String inferAudience(String content) {
        String lowerContent = content.toLowerCase();
        
        if (lowerContent.contains("business") || lowerContent.contains("company")) {
            return "businesses and professionals";
        }
        if (lowerContent.contains("consumer") || lowerContent.contains("customer")) {
            return "consumers and individuals";
        }
        
        return "target market";
    }

    private List<String> extractTopics(List<String> keywords) {
        return keywords.stream()
                .filter(k -> k.length() > 4)
                .limit(4)
                .collect(Collectors.toList());
    }

    private List<String> extractDifferentiators(String content) {
        List<String> diff = new ArrayList<>();
        String lowerContent = content.toLowerCase();
        
        if (lowerContent.contains("quality") || lowerContent.contains("premium")) {
            diff.add("quality focus");
        }
        if (lowerContent.contains("innovation") || lowerContent.contains("technology")) {
            diff.add("innovation");
        }
        if (lowerContent.contains("experience") || lowerContent.contains("expert")) {
            diff.add("expertise");
        }
        
        return diff.isEmpty() ? List.of("unique approach") : diff;
    }

    private String extractBaseUrl(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getScheme() + "://" + uri.getHost();
        } catch (Exception e) {
            return url;
        }
    }

    private WebsiteContext createFallbackContext(String url) {
        String domain = extractBrandName("", url);
        return new WebsiteContext(
                domain,
                List.of("business solutions"),
                "target customers",
                List.of("business", "services"),
                List.of("quality service"),
                List.of("business", "service", "solution"),
                "en"
        );
    }

    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("the", "and", "for", "are", "with", "you", "our", "can", "will", "this", "that", "have", "from", "they", "been", "your", "more", "about", "into", "over", "after");
        return stopWords.contains(word);
    }

    private String truncate(String text, int maxLength) {
        return text != null && text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    private List<String> truncateList(List<String> list, int maxItems, int maxItemLength) {
        return list.stream()
                .limit(maxItems)
                .map(item -> truncate(item, maxItemLength))
                .collect(Collectors.toList());
    }
}