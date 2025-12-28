package com.aivideocoach.agent;

import com.aivideocoach.agent.dto.AgentChatResponse;
import com.aivideocoach.agent.dto.AgentState;
import com.aivideocoach.agent.memory.SessionStateStore;
import com.aivideocoach.agent.tools.InspirationTool;
import com.aivideocoach.youtube.dto.YoutubeInspirationResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class AgentService {

    private final SessionStateStore store;
    private final InspirationTool tools;

    public AgentService(SessionStateStore store, InspirationTool tools) {
        this.store = store;
        this.tools = tools;
    }

    public Mono<AgentChatResponse> chat(String sessionId, String message) {
        AgentState current = store.getOrCreate(sessionId);

        // v1: “איסוף” בסיסי — אפשר לשדרג אח"כ ל-LLM extraction
        AgentState merged = mergeHeuristics(current, message);
        store.put(sessionId, merged);

        List<String> missing = missingFields(merged);

        // אם חסר מידע → לשאול
        if (!missing.isEmpty()) {
            String question = buildQuestion(missing);
            return Mono.just(new AgentChatResponse(
                    sessionId,
                    question,
                    merged,
                    missing,
                    null
            ));
        }

        // יש מספיק → מפעילים tool (YouTube)
        YoutubeInspirationResponse response = tools.topYoutube(
                merged.businessName(),
                merged.domain(),
                merged.targetAudience(),
                merged.keywords(),
                merged.language(),
                merged.location()
        );
        
        Pair pair = successMessage(response);
        return Mono.just(new AgentChatResponse(
                sessionId,
                pair.message(),
                merged,
                List.of(),
                pair.res().results()
        ));

    }

    private record Pair(String message, YoutubeInspirationResponse res) {}

    private Pair successMessage(YoutubeInspirationResponse res) {
        int count = (res.results() == null) ? 0 : res.results().size();

        String msg = "מצאתי " + count + " סרטונים חזקים מה-"
                + res.windowDays() + " ימים האחרונים, ממוינים לפי צפיות. רוצה שאסכם 3 תבניות שחוזרות על עצמן?";
        return new Pair(msg, res);
    }


    private static List<String> missingFields(AgentState s) {
        List<String> missing = new ArrayList<>();
        if (isBlank(s.domain())) missing.add("domain");
        if (isBlank(s.targetAudience())) missing.add("targetAudience");
        if (s.keywords() == null || s.keywords().isEmpty()) missing.add("keywords");
        // language/location הם nice-to-have, לא חובה
        return missing;
    }

    private static String buildQuestion(List<String> missing) {
        // קצר, ממוקד, בעברית
        List<String> qs = new ArrayList<>();
        if (missing.contains("domain")) qs.add("מה התחום המדויק של העסק? (למשל: כושר / ביוטי / נדל\"ן)");
        if (missing.contains("targetAudience")) qs.add("מי קהל היעד? (גיל/מין/מדינה/שפה)");
        if (missing.contains("keywords")) qs.add("תן לי 3–6 מילות מפתח שחשוב לך (למשל: \"אכנה\", \"טיפוח עור\", \"לפני ואחרי\")");
        return String.join("\n", qs);
    }

    private static AgentState mergeHeuristics(AgentState current, String message) {
        // כאן בכוונה פשוט מאוד. בהמשך נעשה extraction עם LLM.
        String m = message == null ? "" : message.trim();

        String domain = current.domain();
        String audience = current.targetAudience();
        List<String> keywords = current.keywords();
        String language = current.language();
        String location = current.location();
        String businessName = current.businessName();

        // דוגמאות “חילוץ” נאיבי:
        if (businessName == null && m.toLowerCase().contains("ai video coach")) businessName = "AI Video Coach";

        // אם המשתמש כתב "בעברית"
        if (language == null && (m.contains("עברית") || m.toLowerCase().contains("hebrew"))) language = "he";
        if (language == null && (m.toLowerCase().contains("english") || m.contains("אנגלית"))) language = "en";

        if (location == null && (m.contains("ישראל") || m.toLowerCase().contains("israel"))) location = "IL";
        if (location == null && (m.toLowerCase().contains("usa") || m.toLowerCase().contains("united states"))) location = "US";

        // keywords: אם המשתמש נתן רשימה עם פסיקים
        if ((keywords == null || keywords.isEmpty()) && m.contains(",")) {
            List<String> parts = Arrays.stream(m.split(","))
                    .map(String::trim).filter(s -> s.length() >= 2).limit(8).toList();
            if (!parts.isEmpty()) keywords = parts;
        }

        // domain: אם המשתמש אומר "אני מאמן כושר" וכו'
        if (domain == null) {
            if (m.contains("כושר") || m.toLowerCase().contains("fitness")) domain = "fitness coaching";
            if (m.contains("ביוטי") || m.contains("קוסמטיקה") || m.toLowerCase().contains("beauty")) domain = "beauty";
        }

        // audience: אם המשתמש כתב "בני נוער" וכו'
        if (audience == null) {
            if (m.contains("בני נוער") || m.contains("נוער")) audience = "teenagers";
            if (m.contains("גברים") && m.contains("30")) audience = "men over 30";
        }

        return new AgentState(businessName, domain, audience, keywords, language, location);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
