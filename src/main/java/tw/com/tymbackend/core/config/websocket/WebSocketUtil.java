package tw.com.tymbackend.core.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
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
public class WebSocketUtil implements WebSocketMessageBrokerConfigurer {

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
    }
} 