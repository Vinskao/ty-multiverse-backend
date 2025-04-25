package tw.com.tymbackend.module.deckofcards.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlackjackController.class)
class BlackjackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getStatus_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/deckofcards/blackjack/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Blackjack game is running"));
    }
} 