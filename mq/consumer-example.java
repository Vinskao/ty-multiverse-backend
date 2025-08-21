package com.example.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DamageCalculationConsumer {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    // 監聽 damage-calculation 隊列
    @RabbitListener(queues = "damage-calculation")
    public void handleDamageCalculation(String message) {
        try {
            // 解析消息
            DamageRequest request = objectMapper.readValue(message, DamageRequest.class);
            
            // 呼叫 Backend Service API
            String backendUrl = "http://ty-multiverse-backend:8080/people/damageWithWeapon";
            ResponseEntity<String> response = restTemplate.postForEntity(
                backendUrl,
                request,
                String.class
            );
            
            // 處理回應
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Damage calculation completed: " + response.getBody());
            } else {
                System.err.println("Damage calculation failed: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            // 可以選擇重新拋出異常讓 RabbitMQ 重新處理
            throw new RuntimeException("Failed to process damage calculation", e);
        }
    }
}

// 請求 DTO
class DamageRequest {
    private String characterName;
    private String weaponName;
    
    // getters and setters
    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }
    public String getWeaponName() { return weaponName; }
    public void setWeaponName(String weaponName) { this.weaponName = weaponName; }
}
