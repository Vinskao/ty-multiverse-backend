package tw.com.tymbackend.module.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
final class ProductIntegrityJob {
    private static final Logger log = LoggerFactory.getLogger(ProductIntegrityJob.class);
    private static final ZoneId TAIPEI = ZoneId.of("Asia/Taipei");
    private final RestClient http;
    private final ObjectMapper json;
    private final String macroUrl;
    private final String token;
    private final String chat;

    ProductIntegrityJob(ObjectMapper json,
        @Value("${product-integrity.macro-url:http://maya-sawa/maya-sawa/market/dashboard/macro}") String macroUrl,
        @Value("${TELEGRAM_BOT_TOKEN:}") String token,
        @Value("${TELEGRAM_CHAT_ID:}") String chat) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        this.http = RestClient.builder().requestFactory(factory).build();
        this.json = json;
        this.macroUrl = macroUrl;
        this.token = token;
        this.chat = chat;
    }

    @Scheduled(cron = "${product-integrity.cron:0 0 11 * * MON}", zone = "Asia/Taipei")
    void checkMacroHistory() {
        LocalDate today = LocalDate.now(TAIPEI);
        List<MacroHistoryCheck.Finding> findings;
        try {
            String body = http.get().uri(macroUrl + "?historyMonths=60").retrieve().body(String.class);
            findings = MacroHistoryCheck.scan(json.readTree(body), today);
        } catch (Exception failure) {
            log.warn("Macro history check could not read its inventory ({})", failure.getClass().getSimpleName());
            send("總經圖資料完整性｜" + today + "\n無法讀取總經指標清單，請檢查資料 API。");
            return;
        }
        var problems = findings.stream().filter(finding -> !finding.expected()).toList();
        long known = findings.size() - problems.size();
        log.info("Macro history check found {} short series ({} known source limits)", findings.size(), known);
        // Known source limits repeat every week; they only ride along as a count when something else is wrong.
        if (problems.isEmpty()) return;
        String heading = "總經圖資料不足三年｜" + today + "｜需處理 " + problems.size() + " 項"
            + (known > 0 ? "（另有 " + known + " 項為已知來源限制，見 maya-sawa docs/MACRO_DATA_INTEGRITY.md）" : "") + "\n";
        var message = new StringBuilder(heading);
        for (var finding : problems) {
            String line = finding.alertLine() + "\n";
            if (message.length() + line.length() > 3800) {
                send(message.toString());
                message = new StringBuilder(heading);
            }
            message.append(line);
        }
        send(message.toString());
    }

    private void send(String message) {
        if (token.isBlank() || chat.isBlank()) {
            log.warn("Product integrity Telegram credentials are missing");
            return;
        }
        try {
            JsonNode result = http.post().uri("https://api.telegram.org/bot" + token + "/sendMessage")
                .body(Map.of("chat_id", chat, "text", message)).retrieve().body(JsonNode.class);
            if (result == null || !result.path("ok").asBoolean()) {
                log.warn("Product integrity Telegram notification was rejected");
            }
        } catch (Exception failure) {
            log.warn("Product integrity Telegram notification failed ({})", failure.getClass().getSimpleName());
        }
    }
}
