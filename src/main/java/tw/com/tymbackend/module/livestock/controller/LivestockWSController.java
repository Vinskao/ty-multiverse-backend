package tw.com.tymbackend.module.livestock.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

@Controller
public class LivestockWSController {
    private static final Logger logger = LoggerFactory.getLogger(LivestockWSController.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public LivestockWSController(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @MessageMapping("/livestock")
    @SendTo("/topic/livestock")
    public String handleLivestockMessage(String message) {
        return "Livestock message received: " + message;
    }

    public void broadcastLivestockUpdate(Livestock livestock) {
        try {
            String livestockJson = objectMapper.writeValueAsString(livestock);
            messagingTemplate.convertAndSend("/topic/livestock", livestockJson);
        } catch (Exception e) {
            logger.error("Error broadcasting livestock update", e);
        }
    }
} 