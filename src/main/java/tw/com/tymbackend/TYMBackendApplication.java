package tw.com.tymbackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import tw.com.tymbackend.config.WebSocketMetricsExporter;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class TYMBackendApplication {
	private static final Logger logger = LoggerFactory.getLogger(TYMBackendApplication.class);

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(TYMBackendApplication.class, args);
		
		// Get the environment to check configurations
		Environment env = context.getEnvironment();
		String managementBasePath = env.getProperty("management.endpoints.web.base-path");
		
		logger.info("Management Base Path: " + managementBasePath);
		
		// 確認 WebSocketMetricsExporter 已被創建並運行
		WebSocketMetricsExporter exporter = context.getBean(WebSocketMetricsExporter.class);
		logger.info("WebSocketMetricsExporter 已成功運行");
	}
}
