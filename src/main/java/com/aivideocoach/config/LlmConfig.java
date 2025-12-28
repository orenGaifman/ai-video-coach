package com.aivideocoach.config;

import com.aivideocoach.agent.llm.InspirationChatAgent;
import com.aivideocoach.agent.llm.VideoContentAgent;
import com.aivideocoach.agent.llm.VideoQualityValidatorAgent;
import com.aivideocoach.agent.tools.InspirationTool;
import com.aivideocoach.agent.tools.WebsiteContextTool;
import com.aivideocoach.agent.tools.VideoQualityValidatorTool;
import com.aivideocoach.agent.tools.VideoAnalysisTool;
import com.aivideocoach.agent.tools.ContentCreationTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LlmConfig {

    @Bean
    public AnthropicChatModel anthropicChatModel(
            @Value("${anthropic.api-key}") String apiKey,
            @Value("${anthropic.model:claude-3-5-haiku-latest}") String modelName
    ) {
        return AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    public InspirationChatAgent inspirationChatAgent(
            AnthropicChatModel model,
            InspirationTool inspirationTool,
            WebsiteContextTool websiteContextTool,
            VideoQualityValidatorTool videoValidatorTool
    ) {
        return AiServices.builder(InspirationChatAgent.class)
                .chatLanguageModel(model)
                .tools(inspirationTool, websiteContextTool, videoValidatorTool)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }

    @Bean
    public VideoContentAgent videoContentAgent(
            AnthropicChatModel model,
            VideoAnalysisTool videoAnalysisTool,
            ContentCreationTool contentCreationTool
    ) {
        return AiServices.builder(VideoContentAgent.class)
                .chatLanguageModel(model)
                .tools(videoAnalysisTool, contentCreationTool)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }

    @Bean
    public VideoQualityValidatorAgent videoQualityValidatorAgent(AnthropicChatModel model) {
        return AiServices.builder(VideoQualityValidatorAgent.class)
                .chatLanguageModel(model)
                .build();
    }
}
