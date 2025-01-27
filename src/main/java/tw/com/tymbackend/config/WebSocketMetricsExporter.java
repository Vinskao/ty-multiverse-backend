package tw.com.tymbackend.config;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.time.Instant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * WebSocketMetricsExporter 類別負責通過 WebSocket 將應用程式的度量數據導出。
 * <p>
 * 它使用 Micrometer 度量庫來收集數據，並通過 STOMP 協議將數據發送到指定的 WebSocket 端點。
 * </p>
 */
@EnableScheduling
@Component
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMetricsExporter implements WebSocketMessageBrokerConfigurer {

    @Value("${keycloak.backend-url}")
    private String keycloakBackendUrl;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketMetricsExporter.class);

    private final ApplicationContext applicationContext;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String actuatorMetricsUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 構造函數，初始化 MeterRegistry 和 ApplicationContext。
     *
     * @param meterRegistry 用於收集度量數據的 MeterRegistry 實例
     * @param applicationContext 用於獲取 Spring 應用上下文的 ApplicationContext 實例
     */
    public WebSocketMetricsExporter(MeterRegistry meterRegistry, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.actuatorMetricsUrl = keycloakBackendUrl + "/actuator/metrics";
    }

    /**
     * 定期導出度量數據到 WebSocket 端點。
     * <p>
     * 每30秒執行一次，將收集的度量數據發送到 "/topic/metrics" 目的地。
     * </p>
     * <p>
     * 該方法會遍歷所有的度量儀表，並將計數器類型的度量數據收集到一個 Map 中。
     * 然後使用 SimpMessagingTemplate 將這些數據廣播到所有連接的客戶端。
     * </p>
     */
    @Scheduled(fixedRate = 5000)
    public void exportSelectedMetrics() {
        logger.info("Executing exportSelectedMetrics method");

        // 定義要收集的度量名稱
        String[] metricNames = {
            "jvm.memory.used",
            "jvm.memory.committed",
            "jvm.memory.max",
            "jvm.gc.live.data.size",
            "jvm.gc.memory.allocated",
            "jvm.gc.memory.promoted",
            "process.cpu.usage",
            "system.cpu.usage",
            "system.cpu.count",
            "disk.free"
        };

        CompletableFuture.supplyAsync(() -> {
            Map<String, Object> metricsData = new HashMap<>();
            for (String metricName : metricNames) {
                try {
                    // 構建請求URL
                    String url = UriComponentsBuilder.fromHttpUrl(actuatorMetricsUrl)
                            .pathSegment(metricName)
                            .toUriString();

                    // 發送HTTP GET請求並獲取響應
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                    if (response != null && response.containsKey("measurements")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> measurements = (List<Map<String, Object>>) response.get("measurements");
                        if (!measurements.isEmpty()) {
                            metricsData.put(metricName, measurements.get(0).get("value"));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error fetching metric: " + metricName, e);
                }
            }
            return metricsData;
        }).thenAccept(metricsData -> {
            try {
                // Add current time to the metrics data
                metricsData.put("timestamp", Instant.now().toString());

                // Convert metrics data to JSON
                String jsonMetricsData = objectMapper.writeValueAsString(metricsData);

                // Log the metrics data
                logger.info("Broadcasting selected metrics data: {}", jsonMetricsData);

                // Obtain SimpMessagingTemplate from the application context
                SimpMessagingTemplate messagingTemplate = applicationContext.getBean(SimpMessagingTemplate.class);
                // Broadcast metrics data to all connected clients
                messagingTemplate.convertAndSend("/topic/metrics", jsonMetricsData);
            } catch (Exception e) {
                logger.error("Error converting metrics data to JSON", e);
            }
        });
    }

    /**
     * 配置消息代理。
     * <p>
     * 該方法設置了簡單的內存消息代理，並定義了應用程序目的地前綴。
     * </p>
     *
     * @param config 用於配置消息代理的 MessageBrokerRegistry 實例
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 註冊 STOMP 端點。
     * <p>
     * 該方法定義了客戶端連接到 WebSocket 的端點，並允許所有來源的跨域請求。
     * </p>
     *
     * @param registry 用於註冊 STOMP 端點的 StompEndpointRegistry 實例
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/metrics").setAllowedOrigins("*").withSockJS();
    }
}
