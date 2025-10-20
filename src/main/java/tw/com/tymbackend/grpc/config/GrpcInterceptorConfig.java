package tw.com.tymbackend.grpc.config;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * gRPC ServerInterceptor 配置类
 *
 * <p>定义所有 gRPC 服务器拦截器，对应 Spring Boot HTTP 层的各种中间件</p>
 *
 * @author TY Team
 * @version 1.0
 */
@Configuration
public class GrpcInterceptorConfig {

    private static final Logger logger = LoggerFactory.getLogger(GrpcInterceptorConfig.class);

    /**
     * gRPC 日志记录拦截器
     * 对应 Spring Boot 的 HandlerInterceptor (preHandle/postHandle)
     */
    @Bean
    public ServerInterceptor loggingInterceptor() {
        return new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers,
                    ServerCallHandler<ReqT, RespT> next) {

                long startTime = System.nanoTime();
                String methodName = call.getMethodDescriptor().getFullMethodName();

                logger.info("📥 gRPC Request: {} from {}",
                    methodName,
                    call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR));

                // 记录请求头信息
                if (logger.isDebugEnabled()) {
                    headers.keys().forEach(key ->
                        logger.debug("Header: {} = {}", key, headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))));
                }

                return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                    next.startCall(call, headers)) {

                    @Override
                    public void onComplete() {
                        long duration = System.nanoTime() - startTime;
                        logger.info("✅ gRPC Response: {} completed in {}ms",
                            methodName, duration / 1_000_000);
                        super.onComplete();
                    }

                    @Override
                    public void onCancel() {
                        logger.warn("⚠️  gRPC Cancelled: {}", methodName);
                        super.onCancel();
                    }
                };
            }
        };
    }

    /**
     * gRPC 认证拦截器
     * 对应 Spring Security Filter
     */
    @Bean
    public ServerInterceptor authInterceptor() {
        return new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers,
                    ServerCallHandler<ReqT, RespT> next) {

                String methodName = call.getMethodDescriptor().getFullMethodName();

                // 检查是否为公开方法
                if (isPublicMethod(methodName)) {
                    logger.debug("🔓 Public gRPC method: {}", methodName);
                    return next.startCall(call, headers);
                }

                // 检查认证头
                String authHeader = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    logger.warn("❌ Missing or invalid auth header for gRPC method: {}", methodName);
                    call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid authorization header"), new Metadata());
                    return new ServerCall.Listener<ReqT>() {};
                }

                String token = authHeader.substring(7); // Remove "Bearer " prefix
                if (!validateToken(token)) {
                    logger.warn("❌ Invalid token for gRPC method: {}", methodName);
                    call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), new Metadata());
                    return new ServerCall.Listener<ReqT>() {};
                }

                logger.debug("✅ Authenticated gRPC call: {}", methodName);
                return next.startCall(call, headers);
            }

            private boolean isPublicMethod(String methodName) {
                // 定义公开的gRPC方法
                return methodName.contains("GetAllPeople") ||
                       methodName.contains("GetPeopleByName") ||
                       methodName.contains("getByName");
            }

            private boolean validateToken(String token) {
                // TODO: 实现JWT token验证逻辑
                // 这里可以调用Keycloak服务进行token验证
                return token != null && token.length() > 10; // 简化的验证
            }
        };
    }

    /**
     * gRPC 错误处理和异常转换拦截器
     * 对应 @ControllerAdvice
     */
    @Bean
    public ServerInterceptor errorHandlingInterceptor() {
        return new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers,
                    ServerCallHandler<ReqT, RespT> next) {

                return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                    next.startCall(call, headers)) {

                    @Override
                    public void onHalfClose() {
                        try {
                            super.onHalfClose();
                        } catch (Exception e) {
                            logger.error("❌ gRPC Error in {}: {}", call.getMethodDescriptor().getFullMethodName(), e.getMessage(), e);
                            call.close(convertExceptionToStatus(e), new Metadata());
                        }
                    }
                };
            }

            private Status convertExceptionToStatus(Exception e) {
                // 将Java异常转换为gRPC Status
                if (e instanceof IllegalArgumentException) {
                    return Status.INVALID_ARGUMENT.withDescription(e.getMessage());
                } else if (e instanceof SecurityException) {
                    return Status.PERMISSION_DENIED.withDescription(e.getMessage());
                } else if (e instanceof UnsupportedOperationException) {
                    return Status.UNIMPLEMENTED.withDescription(e.getMessage());
                } else {
                    return Status.INTERNAL.withDescription("Internal server error");
                }
            }
        };
    }

    /**
     * gRPC 限流拦截器
     * 对应 RequestConcurrencyLimiter
     */
    @Bean
    public ServerInterceptor rateLimitInterceptor() {
        return new ServerInterceptor() {
            private final int MAX_CONCURRENT_REQUESTS = 2; // 与HTTP保持一致
            private final java.util.concurrent.Semaphore semaphore = new java.util.concurrent.Semaphore(MAX_CONCURRENT_REQUESTS, true);

            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers,
                    ServerCallHandler<ReqT, RespT> next) {

                try {
                    if (!semaphore.tryAcquire(200, TimeUnit.MILLISECONDS)) { // 与HTTP保持一致的超时时间
                        logger.warn("⏱️  gRPC Rate limited: {}", call.getMethodDescriptor().getFullMethodName());
                        call.close(Status.RESOURCE_EXHAUSTED.withDescription("Too many concurrent requests"), new Metadata());
                        return new ServerCall.Listener<ReqT>() {};
                    }

                    return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                        next.startCall(call, headers)) {

                        @Override
                        public void onComplete() {
                            semaphore.release();
                            super.onComplete();
                        }

                        @Override
                        public void onCancel() {
                            semaphore.release();
                            super.onCancel();
                        }
                    };

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    call.close(Status.INTERNAL.withDescription("Request interrupted"), new Metadata());
                    return new ServerCall.Listener<ReqT>() {};
                }
            }
        };
    }
}

