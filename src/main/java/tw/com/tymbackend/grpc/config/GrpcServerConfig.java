package tw.com.tymbackend.grpc.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.com.tymbackend.grpc.service.GrpcPeopleServiceImpl;
import tw.com.tymbackend.grpc.service.GrpcWeaponServiceImpl;
import tw.com.tymbackend.grpc.service.GrpcGalleryServiceImpl;
import tw.com.tymbackend.grpc.service.GrpcDeckofcardsServiceImpl;
import tw.com.tymbackend.grpc.service.GrpcCkeditorServiceImpl;

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
    private GrpcWeaponServiceImpl grpcWeaponService;

    @Autowired
    private GrpcGalleryServiceImpl grpcGalleryService;

    @Autowired
    private GrpcDeckofcardsServiceImpl grpcDeckofcardsService;

    @Autowired
    private GrpcCkeditorServiceImpl grpcCkeditorService;

    @Autowired
    private ServerInterceptor loggingInterceptor;

    @Autowired
    private ServerInterceptor authInterceptor;

    @Autowired
    private ServerInterceptor errorHandlingInterceptor;

    @Autowired
    private ServerInterceptor rateLimitInterceptor;

    private Server grpcServer;


    @PostConstruct
    public void start() throws IOException {
        logger.info("🚀 启动 gRPC Server，端口: {}", grpcPort);

        grpcServer = ServerBuilder.forPort(grpcPort)
                // 添加 gRPC Interceptor (按顺序执行)
                .intercept(rateLimitInterceptor)      // 1. 限流拦截器
                .intercept(authInterceptor)           // 2. 认证拦截器
                .intercept(loggingInterceptor)        // 3. 日志记录拦截器
                .intercept(errorHandlingInterceptor)  // 4. 错误处理拦截器
                // 注册服务
                .addService(grpcPeopleService.bindService())
                .addService(grpcWeaponService.bindService())
                .addService(grpcGalleryService.bindService())
                .addService(grpcDeckofcardsService.bindService())
                .addService(grpcCkeditorService.bindService())
                .build()
                .start();

        logger.info("✅ gRPC Server 已启动在端口: {}", grpcPort);
        logger.info("🛡️  已启用 gRPC Interceptor: RateLimit -> Auth -> Logging -> ErrorHandling");
        logger.info("📡 可用服务: PeopleService, WeaponService, GalleryService, DeckofcardsService, CkeditorService");

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
