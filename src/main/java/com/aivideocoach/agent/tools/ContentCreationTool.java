package com.aivideocoach.agent.tools;

import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ContentCreationTool {
    
    private static final Logger log = LoggerFactory.getLogger(ContentCreationTool.class);
    
    @Tool("Provides detailed content creation guidance including tools, timeline, and production steps")
    public String createContentGuide(
            String videoAnalysis,
            String businessType,
            String userGoals
    ) {
        try {
            log.info("Creating content guide for business type: {}, goals: {}", businessType, userGoals);
            
            return generateNaturalContentGuide(videoAnalysis, businessType, userGoals);
            
        } catch (Exception e) {
            log.error("Error creating content guide: {}", e.getMessage(), e);
            return "Error creating content guide: " + e.getMessage();
        }
    }
    
    @Tool("Recommends specific tools and equipment for video production based on budget and skill level")
    public String recommendTools(String budgetLevel, String contentType, String skillLevel) {
        try {
            log.info("Recommending tools for budget: {}, type: {}, level: {}", budgetLevel, contentType, skillLevel);
            
            return generateToolRecommendations(budgetLevel, contentType, skillLevel);
            
        } catch (Exception e) {
            log.error("Error recommending tools: {}", e.getMessage(), e);
            return "Error recommending tools: " + e.getMessage();
        }
    }
    
    private String generateNaturalContentGuide(String videoAnalysis, String businessType, String userGoals) {
        return String.format("""
            CONTENT_CREATION_GUIDE:
            
            PREPARATION_PHASE: Start by researching your specific niche and understanding what your audience truly needs. Study successful videos in your field and identify the gaps you can fill with your unique perspective. Create a detailed script outline that focuses on one main idea, ensuring every sentence adds value. Prepare your filming location with good lighting and minimal distractions, and test all your equipment beforehand to avoid technical issues during recording.
            
            FILMING_PHASE: Set up your camera at eye level and ensure your lighting is soft and even across your face. Record in a quiet environment and speak clearly with energy and enthusiasm. Film multiple takes of important sections so you have options during editing. Use natural gestures and maintain eye contact with the camera to create connection with viewers. Don't forget to record extra B-roll footage that you can use to illustrate your points visually.
            
            EDITING_PHASE: Start by cutting out all unnecessary pauses and filler words to maintain good pacing. Add visual elements like text overlays, graphics, or charts to support your key points and keep viewers engaged. Use music sparingly and ensure it enhances rather than distracts from your message. Create smooth transitions between sections and add subtle zoom-ins or cuts to maintain visual interest throughout the video.
            
            PUBLISHING_PHASE: Write a compelling title that clearly communicates the value viewers will receive while incorporating relevant keywords for discoverability. Create an eye-catching thumbnail that stands out in search results and accurately represents your content. Write a detailed description that includes timestamps, relevant links, and a clear call-to-action. Choose appropriate tags and consider the best time to publish based on when your audience is most active.
            
            TIMELINE_ESTIMATE: Plan for about two weeks from concept to publication if you're working part-time on this project. The research and planning phase typically takes 2-3 days, filming can be completed in 1-2 days depending on complexity, editing usually requires 4-5 days for a polished result, and final optimization and publishing preparation takes another 1-2 days.
            
            BUDGET_CONSIDERATIONS: You can start with minimal investment using your smartphone and free editing software, which might cost under $100 for basic accessories like a tripod and external microphone. For better quality, consider investing $300-500 in a decent camera, lighting setup, and paid editing software. Professional-level production might require $1000+ for high-end equipment, but this is only necessary once you've validated your content strategy and audience engagement.
            
            VIDEO_ANALYSIS: %s
            BUSINESS_TYPE: %s
            USER_GOALS: %s
            """, videoAnalysis, businessType, userGoals);
    }
    
    private String generateToolRecommendations(String budget, String type, String level) {
        return String.format("""
            🛠️ TOOL RECOMMENDATIONS
            
            💰 Budget Level: %s
            🎥 Content Type: %s
            📊 Skill Level: %s
            
            📱 CAMERA/RECORDING:
            
            Beginner/Low Budget:
            • Smartphone with good camera (iPhone 12+, Samsung S21+)
            • Phone tripod mount ($10-20)
            • Ring light for smartphones ($25-50)
            
            Intermediate/Medium Budget:
            • DSLR/Mirrorless camera ($400-800)
            • 50mm or 85mm lens for portraits
            • External microphone (Rode VideoMic GO - $80)
            • LED panel lights ($100-200)
            
            Advanced/High Budget:
            • Professional camera (Sony A7 series, Canon R series)
            • Multiple lenses (wide, standard, telephoto)
            • Professional lighting kit ($500+)
            • Wireless microphone system ($200-500)
            
            🎤 AUDIO EQUIPMENT:
            
            Essential (All Levels):
            • Lavalier microphone ($20-50)
            • Audio recorder app (free) or device
            • Quiet recording environment
            
            Upgrade Options:
            • USB microphone for voiceovers ($50-150)
            • Shotgun microphone ($100-300)
            • Audio interface for professional mics ($100+)
            
            💡 LIGHTING:
            
            Basic Setup:
            • Natural window light (free)
            • Reflector or white poster board ($10)
            • Ring light ($30-100)
            
            Professional Setup:
            • 3-point lighting kit ($200-500)
            • Softboxes and diffusers
            • Background lights for separation
            
            ✂️ EDITING SOFTWARE:
            
            Free Options:
            • DaVinci Resolve (professional-grade, free)
            • iMovie (Mac users)
            • OpenShot (cross-platform)
            
            Paid Options:
            • Adobe Premiere Pro ($20/month)
            • Final Cut Pro ($300 one-time)
            • Filmora ($50/year)
            
            🎨 GRAPHICS & THUMBNAILS:
            
            Free Tools:
            • Canva (templates and easy design)
            • GIMP (advanced photo editing)
            • Unsplash (free stock photos)
            
            Paid Tools:
            • Adobe Photoshop ($20/month)
            • Adobe After Effects (motion graphics)
            • Figma (design and collaboration)
            
            📊 ANALYTICS & OPTIMIZATION:
            
            Essential Tools:
            • YouTube Studio (built-in analytics)
            • TubeBuddy (browser extension - free/paid tiers)
            • VidIQ (competitor analysis - free/paid)
            
            Advanced Tools:
            • Social Blade (channel statistics)
            • Morningfame (growth analytics)
            • Hootsuite (social media scheduling)
            
            🎵 MUSIC & SOUND:
            
            Free Resources:
            • YouTube Audio Library
            • Freesound.org
            • Zapsplat (free with registration)
            
            Paid Resources:
            • Epidemic Sound ($15/month)
            • Artlist ($25/month)
            • AudioJungle (per-track pricing)
            
            📋 WORKFLOW TOOLS:
            
            Planning & Organization:
            • Notion (content planning)
            • Trello (project management)
            • Google Workspace (collaboration)
            
            Script Writing:
            • Google Docs (collaboration)
            • Celtx (screenwriting format)
            • WriterDuet (professional scripts)
            
            💡 QUICK START RECOMMENDATIONS:
            
            Absolute Beginner ($0-100):
            1. Use your smartphone camera
            2. Find good natural lighting
            3. Use a lavalier mic ($25)
            4. Edit with DaVinci Resolve (free)
            5. Create thumbnails with Canva (free)
            
            Ready to Invest ($200-500):
            1. Get a basic DSLR or upgrade phone
            2. Buy a ring light and tripod
            3. Invest in a good USB microphone
            4. Subscribe to Adobe Creative Suite
            5. Get TubeBuddy Pro for optimization
            
            Professional Setup ($1000+):
            1. Professional camera and lenses
            2. 3-point lighting setup
            3. Wireless microphone system
            4. Professional editing software
            5. Analytics and automation tools
            """, budget, type, level);
    }
}