package tw.com.tymbackend.module.palais;

import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "palais.access.enabled", havingValue = "true")
class PalaisFailureNotification {
    record Exhausted(UUID requestId) {}

    private static final Logger log = LoggerFactory.getLogger(PalaisFailureNotification.class);
    private final RestClient http;
    private final String token;
    private final String chat;

    PalaisFailureNotification(@Value("${TELEGRAM_BOT_TOKEN:}") String token,
                              @Value("${TELEGRAM_CHAT_ID:}") String chat) {
        this.token = token;
        this.chat = chat;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.http = RestClient.builder().requestFactory(factory).build();
    }

    @TransactionalEventListener
    void notifyAdministrator(Exhausted failure) {
        if (token.isBlank() || chat.isBlank()) {
            log.warn("Palais provisioning failed for request {}. Telegram credentials are unavailable.",
                    failure.requestId());
            return;
        }
        try {
            http.post().uri("https://api.telegram.org/bot" + token + "/sendMessage")
                    .body(Map.of("chat_id", chat, "text", "Palais 權限同步失敗。請到帳號設定查看審核紀錄。申請 "
                            + failure.requestId()))
                    .retrieve().toBodilessEntity();
        } catch (RuntimeException error) {
            log.warn("Palais failure notification could not be sent for request {}", failure.requestId());
        }
    }
}
