package tw.com.tymbackend.controller;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
 * WebSocketController 負責處理 WebSocket 消息和度量數據的導出。
 * <p>
 * 此類使用 Spring 的 @Scheduled 註解來定期導出選定的度量數據，
 * 並通過 WebSocket 將其廣播給訂閱者。
 * </p>
 */
@EnableScheduling
@Component
public class WebSocketController {

    @Value("${url.address}")
    private String backendUrl;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final ApplicationContext applicationContext;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String actuatorMetricsUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CompletableFuture<String> metricsFuture;

    /**
     * 構造函數，初始化 WebSocketController。
     *
     * @param meterRegistry      度量註冊表，用於度量數據的收集。
     * @param applicationContext 應用程序上下文，用於獲取 Spring beans。
     */
    public WebSocketController(MeterRegistry meterRegistry, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.actuatorMetricsUrl = "http://localhost:8080/tymb/actuator/metrics";
    }

    /**
     * 定期導出選定的度量數據。
     * <p>
     * 此方法每 30 秒執行一次，從 Actuator 獲取指定的度量數據，
     * 並將其轉換為 JSON 格式後通過 WebSocket 廣播。
     * </p>
     */
    @Scheduled(fixedRate = 30000)
    public void exportSelectedMetrics() {
        // logger.info("Executing exportSelectedMetrics method");

        // 定義要導出的度量名稱
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

        // 使用 CompletableFuture 非同步地獲取度量數據
        metricsFuture = CompletableFuture.supplyAsync(() -> {
            Map<String, Object> metricsData = new HashMap<>();
            for (String metricName : metricNames) {
                try {
                    // 構建度量 URL
                    String url = UriComponentsBuilder.fromHttpUrl(actuatorMetricsUrl)
                            .pathSegment(metricName)
                            .toUriString();

                    // 發送 GET 請求以獲取度量數據
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                    // 如果響應包含測量數據，則提取第一個值
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
        }).thenApply(metricsData -> {
            try {
                // 添加時間戳到度量數據
                metricsData.put("timestamp", Instant.now().toString());
                // 將度量數據轉換為 JSON 字符串
                return objectMapper.writeValueAsString(metricsData);
            } catch (Exception e) {
                logger.error("Error converting metrics data to JSON", e);
                return null;
            }
        });

        // 當度量數據準備好後，通過 WebSocket 廣播
        metricsFuture.thenAccept(cachedMetricsData -> {
            if (cachedMetricsData != null) {
                // logger.info("<Before> Broadcasting selected metrics data: {}", cachedMetricsData);

                // 獲取 SimpMessagingTemplate 並發送消息
                SimpMessagingTemplate messagingTemplate = applicationContext.getBean(SimpMessagingTemplate.class);
                messagingTemplate.convertAndSend("/topic/metrics", cachedMetricsData);

                // logger.info("<After> Broadcasted selected metrics data: {}", cachedMetricsData);
            }
        });
    }

    /**
     * 處理 WebSocket 消息。
     * <p>
     * 當接收到消息時，記錄消息並返回確認。
     * </p>
     *
     * @param message 接收到的消息。
     * @return 確認消息。
     */
    @MessageMapping("/")
    @SendTo("/topic/metrics")
    public String handleMetricsMessage(String message) {
        // logger.info("Received message: {}", message);
        return "Message received: " + message;
    }
} 