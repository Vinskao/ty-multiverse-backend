package com.example.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class RabbitMQConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitMQConsumerApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

@Component
class DamageCalculationConsumer {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    // 監聽傷害計算隊列
    @RabbitListener(queues = "damage-calculation")
    public void handleDamageCalculation(String message) {
        try {
            System.out.println("收到傷害計算請求: " + message);
            
            // 解析消息
            JsonNode messageNode = objectMapper.readTree(message);
            String characterName = messageNode.get("payload").asText();
            String requestId = messageNode.get("requestId").asText();
            
            // 呼叫 Backend Service API
            String backendUrl = "http://ty-multiverse-backend:8080/tymb/people/damageWithWeapon?name=" + characterName;
            ResponseEntity<String> response = restTemplate.getForEntity(backendUrl, String.class);
            
            // 處理回應
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("傷害計算完成 - 角色: " + characterName + ", 結果: " + response.getBody());
                
                // 這裡可以將結果存儲到 Redis 或數據庫，供前端查詢
                storeResult(requestId, response.getBody());
                
            } else {
                System.err.println("傷害計算失敗 - 角色: " + characterName + ", 狀態: " + response.getStatusCode());
                storeError(requestId, "計算失敗: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("處理傷害計算消息時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void storeResult(String requestId, String result) {
        // 將結果存儲到 Redis 或數據庫
        // 這裡只是示例，實際實現需要根據您的存儲方案
        System.out.println("存儲結果 - RequestId: " + requestId + ", Result: " + result);
    }
    
    private void storeError(String requestId, String error) {
        // 將錯誤存儲到 Redis 或數據庫
        System.out.println("存儲錯誤 - RequestId: " + requestId + ", Error: " + error);
    }
}

@Component
class PeopleGetAllConsumer {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    // 監聽角色列表獲取隊列
    @RabbitListener(queues = "people-get-all")
    public void handlePeopleGetAll(String message) {
        try {
            System.out.println("收到角色列表獲取請求: " + message);
            
            // 解析消息
            JsonNode messageNode = objectMapper.readTree(message);
            String requestId = messageNode.get("requestId").asText();
            
            // 呼叫 Backend Service API
            String backendUrl = "http://ty-multiverse-backend:8080/tymb/people/get-all";
            ResponseEntity<String> response = restTemplate.postForEntity(backendUrl, null, String.class);
            
            // 處理回應
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("角色列表獲取完成 - 結果: " + response.getBody());
                
                // 存儲結果
                storeResult(requestId, response.getBody());
                
            } else {
                System.err.println("角色列表獲取失敗 - 狀態: " + response.getStatusCode());
                storeError(requestId, "獲取失敗: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("處理角色列表獲取消息時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void storeResult(String requestId, String result) {
        // 將結果存儲到 Redis 或數據庫
        System.out.println("存儲角色列表結果 - RequestId: " + requestId + ", Result: " + result);
    }
    
    private void storeError(String requestId, String error) {
        // 將錯誤存儲到 Redis 或數據庫
        System.out.println("存儲角色列表錯誤 - RequestId: " + requestId + ", Error: " + error);
    }
}
