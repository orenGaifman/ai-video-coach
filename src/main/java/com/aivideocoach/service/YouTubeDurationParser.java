package com.aivideocoach.service;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class YouTubeDurationParser {

    // YouTube uses ISO 8601 duration format: PT4M13S, PT1H2M10S, etc.
    private static final Pattern DURATION_PATTERN = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?");

    public Integer parseToSeconds(String isoDuration) {
        if (isoDuration == null || isoDuration.isBlank()) {
            return null;
        }

        Matcher matcher = DURATION_PATTERN.matcher(isoDuration);
        if (!matcher.matches()) {
            return null;
        }

        int hours = parseGroup(matcher.group(1));
        int minutes = parseGroup(matcher.group(2));
        int seconds = parseGroup(matcher.group(3));

        return hours * 3600 + minutes * 60 + seconds;
    }

    private int parseGroup(String group) {
        return group != null ? Integer.parseInt(group) : 0;
    }
}