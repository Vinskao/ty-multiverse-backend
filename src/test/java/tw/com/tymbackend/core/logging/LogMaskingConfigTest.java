package tw.com.tymbackend.core.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.ConsoleAppender;

/**
 * The application's logback-spring.xml must route every console line through the masking converters from
 * ty-multiverse-common, so credentials never reach the pod logs, whatever code logged them.
 */
class LogMaskingConfigTest {

    @Test
    void consoleOutput_Should_MaskSecretsInMessageAndStackTrace() throws Exception {
        URL config = getClass().getClassLoader().getResource("logback-spring.xml");
        assertThat(config).as("src/main/resources/logback-spring.xml").isNotNull();

        LoggerContext context = new LoggerContext();
        context.putProperty("LOG_FILE", "target/log-masking-test.log");
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(config);

        @SuppressWarnings("unchecked")
        ConsoleAppender<?> console = (ConsoleAppender<?>) context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("CONSOLE");
        assertThat(console).isNotNull();
        var layout = ((PatternLayoutEncoder) console.getEncoder()).getLayout();

        Exception error = new IllegalStateException("query failed jdbc:postgresql://admin:hunter2@db/people",
                new RuntimeException("introspect Bearer abc.def.ghi"));
        // built with setters: works on logback 1.4 (Boot 3.2) and 1.5 without an MDC adapter
        LoggingEvent event = new LoggingEvent();
        event.setLoggerName("test");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setLevel(Level.ERROR);
        event.setThreadName("main");
        event.setMDCPropertyMap(java.util.Map.of());
        event.setTimeStamp(System.currentTimeMillis());
        event.setMessage("login failed for bob@example.com refreshToken=rt-123");
        event.setThrowableProxy(new ThrowableProxy(error));
        String line = layout.doLayout(event);

        assertThat(line).contains("IllegalStateException").contains("Caused by")
                .doesNotContain("hunter2").doesNotContain("abc.def.ghi")
                .doesNotContain("rt-123").doesNotContain("bob@example.com");
        context.stop();
    }
}
