package tw.com.tymbackend.module.ai_usage.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalCcusageStartupSync {

    private static final Logger logger = LoggerFactory.getLogger(LocalCcusageStartupSync.class);

    @Value("${ai-usage.local-startup-sync.enabled:true}")
    private boolean enabled;

    @EventListener(ApplicationReadyEvent.class)
    public void syncAfterStartup() {
        if (!enabled) {
            return;
        }

        Thread.ofVirtual().name("ccusage-startup-sync").start(this::runSync);
    }

    private void runSync() {
        Path projectDir = Path.of(System.getProperty("user.dir"));
        Path script = projectDir.resolve("scripts/sync_ccusage_to_db.py");
        if (!Files.isRegularFile(script)) {
            logger.warn("Skipping ccusage startup sync: script not found at {}", script);
            return;
        }

        logger.info("Starting local ccusage sync");
        try {
            Process process = new ProcessBuilder("python3", script.toString())
                .directory(projectDir.toFile())
                .redirectErrorStream(true)
                .start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(line -> logger.info("[ccusage] {}", line));
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Local ccusage sync completed");
            } else {
                logger.warn("Local ccusage sync exited with code {}", exitCode);
            }
        } catch (Exception e) {
            logger.warn("Local ccusage sync failed: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
