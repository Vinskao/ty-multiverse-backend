package tw.com.tymbackend.module.deckofcards.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import tw.com.tymbackend.core.exception.ErrorCode;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Controller for Blackjack game with Session support
 */
@RestController
@RequestMapping("/deckofcards/blackjack")
public class BlackjackController {

    /**
     * Get the status of the Blackjack game
     * 
     * @return the status of the game
     */
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Blackjack game is running");
    }

    /**
     * 開始新遊戲
     * 
     * @param session HTTP 會話
     * @return 遊戲狀態
     */
    @PostMapping("/start")
    public ResponseEntity<?> startGame(HttpSession session) {
        if (session == null) {
            return ResponseEntity.status(401).body(ErrorCode.SESSION_NOT_FOUND.getMessage());
        }

        // 初始化遊戲狀態
        Map<String, Object> gameState = new HashMap<>();
        gameState.put("game_started", System.currentTimeMillis());
        gameState.put("player_hand", new ArrayList<String>());
        gameState.put("dealer_hand", new ArrayList<String>());
        gameState.put("game_status", "active");
        gameState.put("player_score", 0);
        gameState.put("dealer_score", 0);

        session.setAttribute("blackjack_game_state", gameState);
        session.setAttribute("last_game_action", "start");

        return ResponseEntity.ok("Game started successfully");
    }

    /**
     * 獲取當前遊戲狀態
     * 
     * @param session HTTP 會話
     * @return 遊戲狀態
     */
    @GetMapping("/state")
    public ResponseEntity<?> getGameState(HttpSession session) {
        if (session == null) {
            return ResponseEntity.status(401).body(ErrorCode.SESSION_NOT_FOUND.getMessage());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> gameState = (Map<String, Object>) session.getAttribute("blackjack_game_state");
        
        if (gameState == null) {
            return ResponseEntity.ok(ErrorCode.NO_ACTIVE_GAME.getMessage());
        }

        return ResponseEntity.ok(gameState);
    }

    /**
     * 玩家抽牌
     * 
     * @param session HTTP 會話
     * @return 抽牌結果
     */
    @PostMapping("/hit")
    public ResponseEntity<?> playerHit(HttpSession session) {
        if (session == null) {
            return ResponseEntity.status(401).body(ErrorCode.SESSION_NOT_FOUND.getMessage());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> gameState = (Map<String, Object>) session.getAttribute("blackjack_game_state");
        
        if (gameState == null) {
            return ResponseEntity.badRequest().body(ErrorCode.NO_ACTIVE_GAME.getMessage());
        }

        // 模擬抽牌邏輯
        @SuppressWarnings("unchecked")
        List<String> playerHand = (List<String>) gameState.get("player_hand");
        playerHand.add("A♠"); // 模擬抽到一張牌
        
        int playerScore = (Integer) gameState.get("player_score") + 11;
        gameState.put("player_score", playerScore);
        gameState.put("last_action", "hit");

        session.setAttribute("blackjack_game_state", gameState);
        session.setAttribute("last_game_action", "hit");

        return ResponseEntity.ok(gameState);
    }

    /**
     * 玩家停牌
     * 
     * @param session HTTP 會話
     * @return 遊戲結果
     */
    @PostMapping("/stand")
    public ResponseEntity<?> playerStand(HttpSession session) {
        if (session == null) {
            return ResponseEntity.status(401).body(ErrorCode.SESSION_NOT_FOUND.getMessage());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> gameState = (Map<String, Object>) session.getAttribute("blackjack_game_state");
        
        if (gameState == null) {
            return ResponseEntity.badRequest().body(ErrorCode.NO_ACTIVE_GAME.getMessage());
        }

        // 模擬莊家抽牌
        @SuppressWarnings("unchecked")
        List<String> dealerHand = (List<String>) gameState.get("dealer_hand");
        dealerHand.add("K♥");
        
        int dealerScore = (Integer) gameState.get("dealer_score") + 10;
        gameState.put("dealer_score", dealerScore);
        gameState.put("game_status", "finished");
        gameState.put("last_action", "stand");

        // 判斷勝負
        int playerScore = (Integer) gameState.get("player_score");
        String result = playerScore > dealerScore ? "Player wins!" : 
                       playerScore < dealerScore ? "Dealer wins!" : "Tie!";
        gameState.put("result", result);

        session.setAttribute("blackjack_game_state", gameState);
        session.setAttribute("last_game_action", "stand");

        return ResponseEntity.ok(gameState);
    }

    /**
     * 結束遊戲
     * 
     * @param session HTTP 會話
     * @return 結束確認
     */
    @PostMapping("/end")
    public ResponseEntity<?> endGame(HttpSession session) {
        if (session == null) {
            return ResponseEntity.status(401).body(ErrorCode.SESSION_NOT_FOUND.getMessage());
        }

        session.removeAttribute("blackjack_game_state");
        session.setAttribute("last_game_action", "end");

        return ResponseEntity.ok("Game ended");
    }
} 