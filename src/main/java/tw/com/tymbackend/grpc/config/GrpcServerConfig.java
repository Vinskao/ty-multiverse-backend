package tw.com.tymbackend.grpc.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.com.tymbackend.grpc.service.GrpcPeopleServiceImpl;
import tw.com.tymbackend.grpc.service.GrpcKeycloakServiceImpl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

/**
 * gRPC Server 配置类
 * 
 * <p>启动 gRPC Server 并注册服务</p>
 * 
 * @author TY Team
 * @version 1.0
 */
@Configuration
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true")
public class GrpcServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServerConfig.class);

    @Value("${grpc.port:50051}")
    private int grpcPort;

    @Autowired
    private GrpcPeopleServiceImpl grpcPeopleService;

    @Autowired
    private GrpcKeycloakServiceImpl grpcKeycloakService;

    private Server grpcServer;

    @PostConstruct
    public void start() throws IOException {
        logger.info("🚀 启动 gRPC Server，端口: {}", grpcPort);
        
        grpcServer = ServerBuilder.forPort(grpcPort)
                .addService(grpcPeopleService.bindService())
                .addService(grpcKeycloakService.bindService())
                .build()
                .start();
        
        logger.info("✅ gRPC Server 已启动在端口: {}", grpcPort);
        logger.info("📡 可用服务: PeopleService, KeycloakService");
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("⏹️  关闭 gRPC Server...");
            GrpcServerConfig.this.stop();
            logger.info("✅ gRPC Server 已关闭");
        }));
    }

    @PreDestroy
    public void stop() {
        if (grpcServer != null && !grpcServer.isShutdown()) {
            grpcServer.shutdown();
        }
    }

    @Bean
    public Server grpcServer() {
        return grpcServer;
    }
}
