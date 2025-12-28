package com.aivideocoach.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cors = new CorsConfiguration();

        // הכי חשוב: לאפשר Vercel (כולל preview deployments) + localhost לפיתוח
        cors.setAllowedOriginPatterns(List.of(
                "https://ai-video-coach-ui.vercel.app",
                "https://*.vercel.app",
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));

        // אם אתה לא משתמש בקוקיז/סשן בין דומיינים — אפשר להשאיר false.
        // אם כן, צריך גם allowedOrigins מדויק ולא patterns + withCredentials בצד UI.
        cors.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);

        return new CorsWebFilter(source);
    }
}
