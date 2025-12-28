import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DateTest {
    public static void main(String[] args) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime date180 = now.minusDays(180);
        OffsetDateTime date30 = now.minusDays(30);
        
        System.out.println("Current UTC time: " + now);
        System.out.println("180 days ago: " + date180);
        System.out.println("30 days ago: " + date30);
        System.out.println("180 days ago formatted: " + date180.toString());
    }
}