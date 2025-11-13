package tw.com.tymbackend.core.config.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket 工具類，負責配置 WebSocket 和 STOMP 端點
 * 
 * 提供 WebSocket 連接管理、消息發送等功能。
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocket
public class WebSocketUtil implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    @Value("${METRICS_BASE_URL:http://localhost:8080/tymb/actuator/metrics}")
    private static String metricsBaseUrl;

    // 線上 Session 管理
    private static final Map<String, Session> ONLINE_SESSION = new ConcurrentHashMap<>();

    /**
     * 新增記錄 Session
     * 
     * @param userNick 用戶暱稱
     * @param session WebSocket Session
     */
    public static void addSession(String userNick, Session session) {
        ONLINE_SESSION.put(userNick, session);
    }

    /**
     * 移除 Session
     * 
     * @param userNick 用戶暱稱
     */
    public static void removeSession(String userNick) {
        ONLINE_SESSION.remove(userNick);
    }

    /**
     * 發送訊息給指定 Session
     * 
     * @param session WebSocket Session
     * @param message 要發送的消息
     */
    public static void sendMessage(Session session, String message) {
        if (session == null) {
            return;
        }
        // 發送訊息
        session.getAsyncRemote().sendText(message);
    }

    /**
     * 發送群體訊息給所有線上用戶
     * 
     * @param message 要發送的消息
     */
    public static void sendMessageForAll(String message) {
        // 使用 Stream API 發送群體訊息
        ONLINE_SESSION.values().stream()
            .filter(session -> session != null && session.isOpen())
            .forEach(session -> sendMessage(session, message));
    }

    /**
     * 獲取線上用戶列表
     * 
     * @return 線上用戶暱稱列表
     */
    public static java.util.List<String> getOnlineUsers() {
        return ONLINE_SESSION.entrySet().stream()
            .filter(entry -> entry.getValue() != null && entry.getValue().isOpen())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * 獲取線上用戶數量
     * 
     * @return 線上用戶數量
     */
    public static long getOnlineUserCount() {
        return ONLINE_SESSION.values().stream()
            .filter(session -> session != null && session.isOpen())
            .count();
    }

    /**
     * 配置消息代理
     *
     * @param config 用於配置消息代理的 MessageBrokerRegistry 實例
     */
    @Override
    public void configureMessageBroker(@SuppressWarnings("null") MessageBrokerRegistry config) {
        // 啟用簡單代理
        config.enableSimpleBroker("/topic");
        // 設置應用程序目的地前綴
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 註冊 STOMP 端點
     *
     * @param registry 用於註冊 STOMP 端點的 StompEndpointRegistry 實例
     */
    @Override
    public void registerStompEndpoints(@SuppressWarnings("null") StompEndpointRegistry registry) {
        // 註冊 STOMP 端點，允許跨域訪問
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
        
        // 同時支援原生 WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");
    }

    /**
     * 註冊 WebSocket 處理器
     *
     * @param registry WebSocket 處理器註冊表
     */
    @Override
    public void registerWebSocketHandlers(@SuppressWarnings("null") WebSocketHandlerRegistry registry) {
        // 註冊專門的 metrics WebSocket 端點
        registry.addHandler(new MetricsWebSocketHandler(), "/metrics")
                .setAllowedOrigins("*");
    }

    /**
     * Metrics WebSocket 處理器
     */
    public static class MetricsWebSocketHandler extends TextWebSocketHandler {
        
        private static final java.util.Set<WebSocketSession> sessions = java.util.concurrent.ConcurrentHashMap.newKeySet();
        private static final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            sessions.add(session);
            // 連接建立時發送歡迎消息
            session.sendMessage(new TextMessage("{\"type\":\"connection\",\"message\":\"Connected to metrics WebSocket\",\"timestamp\":\"" + java.time.Instant.now() + "\"}"));
            
            // 立即發送一次 metrics 數據
            sendMetricsToSession(session);
        }
        
        @Override
        public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
            sessions.remove(session);
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            // 處理收到的消息
            String payload = message.getPayload();
            
            if ("get-metrics".equals(payload) || "{\"type\":\"get-metrics\"}".equals(payload)) {
                // 發送 metrics 數據
                sendMetricsToSession(session);
            } else {
                // 發送確認消息
                String response = "{\"type\":\"ack\",\"message\":\"Message received\",\"payload\":\"" + payload + "\",\"timestamp\":\"" + java.time.Instant.now() + "\"}";
                session.sendMessage(new TextMessage(response));
            }
        }
        
        /**
         * 發送 metrics 數據到指定 session
         */
        private void sendMetricsToSession(WebSocketSession session) {
            try {
                String metricsData = getMetricsData();
                session.sendMessage(new TextMessage(metricsData));
            } catch (Exception e) {
                try {
                    String errorResponse = "{\"type\":\"error\",\"message\":\"Failed to get metrics\",\"error\":\"" + e.getMessage() + "\",\"timestamp\":\"" + java.time.Instant.now() + "\"}";
                    session.sendMessage(new TextMessage(errorResponse));
                } catch (Exception ex) {
                    // 忽略發送錯誤消息時的異常
                }
            }
        }
        
        /**
         * 獲取 metrics 數據
         */
        private String getMetricsData() throws Exception {
            String baseUrl = metricsBaseUrl;
            
            // 定義要獲取的 metrics
            String[] metrics = {
                "jvm.memory.used",
                "process.cpu.usage", 
                "hikaricp.connections.active",
                "http.server.requests",
                "jvm.threads.live",
                "system.cpu.usage"
            };
            
            java.util.Map<String, Object> metricsData = new java.util.HashMap<>();
            metricsData.put("type", "metrics");
            metricsData.put("timestamp", java.time.Instant.now().toString());
            metricsData.put("data", new java.util.HashMap<>());
            
            for (String metric : metrics) {
                try {
                    String url = baseUrl + "/" + metric;
                    String response = restTemplate.getForObject(url, String.class);
                    
                    if (response != null) {
                        java.util.Map<String, Object> metricData = objectMapper.readValue(response, java.util.Map.class);
                        ((java.util.Map<String, Object>) metricsData.get("data")).put(metric, metricData);
                    }
                } catch (Exception e) {
                    // 記錄錯誤但繼續處理其他 metrics
                    ((java.util.Map<String, Object>) metricsData.get("data")).put(metric, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            }
            
            return objectMapper.writeValueAsString(metricsData);
        }
        
        /**
         * 廣播 metrics 數據到所有連接的 session
         */
        public static void broadcastMetrics() {
            final String metricsData = getMetricsDataForBroadcast();
            
            sessions.removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(metricsData));
                        return false;
                    }
                } catch (Exception e) {
                    // 發送失敗，移除 session
                }
                return true;
            });
        }
        
        /**
         * 獲取用於廣播的 metrics 數據
         */
        private static String getMetricsDataForBroadcast() {
            try {
                return new MetricsWebSocketHandler().getMetricsData();
            } catch (Exception e) {
                return "{\"type\":\"error\",\"message\":\"Failed to get metrics\",\"error\":\"" + e.getMessage() + "\",\"timestamp\":\"" + java.time.Instant.now() + "\"}";
            }
        }
    }
} 