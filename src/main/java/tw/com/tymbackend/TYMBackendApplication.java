package tw.com.tymbackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@SpringBootApplication
@EnableWebSocket
public class TYMBackendApplication {
	private static final Logger logger = LoggerFactory.getLogger(TYMBackendApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(TYMBackendApplication.class, args);
	}
    /**
     * 自動註冊使用了@ServerEndpoint註解聲明的Websocket endpoint      
     * 注意：如果使用獨立的servlet容器，而不是直接使用springboot的內置容器
     * 請不要注入ServerEndpointExporter，因為它將由容器自己提供和管理。    
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
