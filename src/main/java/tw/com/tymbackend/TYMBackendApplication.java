package tw.com.tymbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import tw.com.tymbackend.config.WebSocketMetricsExporter;

@SpringBootApplication
public class TYMBackendApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(TYMBackendApplication.class, args);
		
		// 確認 WebSocketMetricsExporter 已被創建並運行
		WebSocketMetricsExporter exporter = context.getBean(WebSocketMetricsExporter.class);
		if (exporter != null) {
			System.out.println("WebSocketMetricsExporter 已成功運行");
		} else {
			System.out.println("WebSocketMetricsExporter 未能運行");
		}
	}
}
