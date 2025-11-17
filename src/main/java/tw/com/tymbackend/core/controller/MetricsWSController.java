package tw.com.tymbackend.core.controller;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;
import java.time.Duration;
import java.util.Arrays;

import tw.com.tymbackend.core.util.DistributedLockUtil;

/**
 * WebSocket 控制器，負責處理 WebSocket 消息和度量數據的導出
 * 
 * 此類使用 Spring 的 @Scheduled 註解來定期導出選定的度量數據，
 * 並通過 WebSocket 將其廣播給訂閱者。使用分布式鎖防止多個實例同時執行。
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>定期導出度量數據</li>
 *   <li>通過 WebSocket 廣播數據</li>
 *   <li>處理度量數據消息</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@EnableScheduling
@Component
public class MetricsWSController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsWSController.class);

    private final ApplicationContext applicationContext;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String actuatorMetricsUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile CompletableFuture<String> metricsFuture;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private DistributedLockUtil distributedLockUtil;
    
    // 從配置文件讀取度量數據導出配置
    @Value("${scheduling.tasks.metrics-export.enabled:true}")
    private boolean metricsExportEnabled;

    @Value("${scheduling.tasks.metrics-export.fixed-rate}")
    private long metricsExportFixedRate;

    @Value("${scheduling.tasks.metrics-export.lock-timeout}")
    private int metricsExportLockTimeout;

    /**
     * 建構函數，初始化 WebSocket 控制器
     *
     * @param meterRegistry 度量註冊表，用於度量數據的收集
     * @param applicationContext 應用程序上下文，用於獲取 Spring beans
     * @param metricsBaseUrl 度量基礎 URL，用於度量數據收集
     */
    public MetricsWSController(
            MeterRegistry meterRegistry,
            ApplicationContext applicationContext,
            @Value("${METRICS_BASE_URL:http://localhost:8080/tymb/actuator/metrics}") String metricsBaseUrl) {
        this.applicationContext = applicationContext;
        this.actuatorMetricsUrl = metricsBaseUrl;
        logger.info("Actuator 度量 URL 初始化為: {}", actuatorMetricsUrl);
    }

    /**
     * 定期導出選定的度量數據
     *
     * 此方法從 Actuator 獲取指定的度量數據，
     * 並將其轉換為 JSON 格式後通過 WebSocket 廣播。
     * 使用分布式鎖防止多個實例同時執行。
     *
     * 可以通過配置 `scheduling.tasks.metrics-export.enabled=false` 來關閉此功能。
     */
    @Scheduled(fixedRateString = "${scheduling.tasks.metrics-export.fixed-rate}")
    public void exportSelectedMetrics() {
        // 檢查是否啟用了度量數據導出功能
        if (!metricsExportEnabled) {
            logger.debug("度量數據導出功能已通過配置關閉");
            return;
        }

        String lockKey = "metrics:export:lock";
        Duration lockTimeout = Duration.ofSeconds(metricsExportLockTimeout);

        try {
            distributedLockUtil.executeWithLock(lockKey, lockTimeout, () -> {
                performMetricsExport();

                // 同時廣播到原生 WebSocket 端點
                try {
                    tw.com.tymbackend.core.config.websocket.WebSocketUtil.MetricsWebSocketHandler.broadcastMetrics();
                    logger.debug("已廣播 metrics 到原生 WebSocket 端點");
                } catch (Exception e) {
                    logger.warn("廣播到原生 WebSocket 失敗", e);
                }

                return null;
            });
        } catch (Exception e) {
            logger.error("度量數據導出失敗", e);
        }
    }
    
    /**
     * 執行度量數據導出的具體邏輯
     * 
     * 從 Actuator 端點獲取指定的度量數據，包括：
     * <ul>
     *   <li>JVM 內存使用情況</li>
     *   <li>系統 CPU 使用率</li>
     *   <li>HTTP 請求統計</li>
     *   <li>數據庫連接池狀態</li>
     * </ul>
     * 
     * @return 度量數據的 JSON 字符串
     */
    private String performMetricsExport() {
        try {
            // 只使用健康檢查端點，因為其他 metrics 端點已被禁用
            String healthUrl = actuatorMetricsUrl.replace("/metrics", "/health");
            
            Map<String, Object> metricsData = new HashMap<>();
            
            try {
                // 檢查 URL 是否有效
                if (healthUrl == null || healthUrl.contains("@")) {
                    logger.error("Health URL 配置錯誤: {}", healthUrl);
                    return "{\"error\": \"Health URL 配置錯誤\"}";
                }
                
                logger.info("請求健康檢查數據 URL: {} (actuatorMetricsUrl: {})", healthUrl, actuatorMetricsUrl);
                
                // 添加請求超時和錯誤處理
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                ResponseEntity<String> response = restTemplate.exchange(
                    healthUrl, 
                    HttpMethod.GET, 
                    entity, 
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> healthData = objectMapper.readValue(response.getBody(), Map.class);
                    metricsData.put("health", healthData);
                    logger.debug("成功獲取健康檢查數據");
                } else {
                    logger.warn("健康檢查請求失敗 - 狀態碼: {}", response.getStatusCode());
                }
            } catch (HttpClientErrorException.NotFound e) {
                logger.warn("健康檢查端點不存在 - URL: {} - 錯誤: {}", healthUrl, e.getMessage());
            } catch (HttpClientErrorException e) {
                logger.warn("HTTP 錯誤 - URL: {} - 狀態碼: {} - 錯誤: {}", healthUrl, e.getStatusCode(), e.getMessage());
            } catch (Exception e) {
                logger.warn("獲取健康檢查數據失敗 - URL: {} - 錯誤: {}", healthUrl, e.getMessage());
            }
            
            // 通過 WebSocket 廣播度量數據
            if (!metricsData.isEmpty()) {
                String jsonData = objectMapper.writeValueAsString(metricsData);
                logger.debug("廣播度量數據: {}", jsonData);
                
                // 這裡可以添加 WebSocket 廣播邏輯
                // messagingTemplate.convertAndSend("/topic/metrics", jsonData);
                
                return jsonData;
            }
            
            return "{}";
            
        } catch (Exception e) {
            logger.error("執行度量數據導出時發生錯誤", e);
            return "{\"error\": \"導出度量數據失敗\"}";
        }
    }

    /**
     * 處理 WebSocket 度量消息
     * 
     * 接收客戶端發送的度量數據請求，並返回相應的度量數據。
     * 
     * @param message 客戶端發送的消息
     * @return 度量數據的 JSON 字符串
     */
    @MessageMapping("/")
    @SendTo("/topic/metrics")
    public String handleMetricsMessage(String message) {
        logger.debug("收到度量數據請求: {}", message);
        
        // 使用 CompletableFuture 非同步地獲取度量數據
        metricsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return performMetricsExport();
            } catch (Exception e) {
                logger.error("處理度量數據請求時發生錯誤", e);
                return "{\"error\": \"獲取度量數據失敗\"}";
            }
        });
        
        try {
            return metricsFuture.get();
        } catch (Exception e) {
            logger.error("等待度量數據時發生錯誤", e);
            return "{\"error\": \"處理請求失敗\"}";
        }
    }
} 