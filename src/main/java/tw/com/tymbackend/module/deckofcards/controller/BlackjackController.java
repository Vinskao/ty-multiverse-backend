package tw.com.tymbackend.module.deckofcards.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Blackjack game
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
} 