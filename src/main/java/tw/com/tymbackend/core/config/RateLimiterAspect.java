package tw.com.tymbackend.core.config;

import io.github.bucket4j.Bucket;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tw.com.tymbackend.core.exception.ResilienceException;

/**
 * Rate Limiter AOP切面
 * 
 * 提供輕量級的 Rate Limiter 保護，防止 DDOS 攻擊
 */
@Aspect
@Component
public class RateLimiterAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);

    private final Bucket apiRateLimiter;
    private final Bucket batchApiRateLimiter;

    public RateLimiterAspect(@Qualifier("apiRateLimiter") Bucket apiRateLimiter,
                            @Qualifier("batchApiRateLimiter") Bucket batchApiRateLimiter) {
        this.apiRateLimiter = apiRateLimiter;
        this.batchApiRateLimiter = batchApiRateLimiter;
    }

    /**
     * 攔截所有people相關的API請求
     */
    @Around("execution(* tw.com.tymbackend.module.people.controller.*.*(..))")
    public Object rateLimitPeopleApis(ProceedingJoinPoint joinPoint) throws Throwable {
        return applyRateLimit(joinPoint, apiRateLimiter, "People API");
    }

    /**
     * 攔截所有weapon相關的API請求
     */
    @Around("execution(* tw.com.tymbackend.module.weapon.controller.*.*(..))")
    public Object rateLimitWeaponApis(ProceedingJoinPoint joinPoint) throws Throwable {
        return applyRateLimit(joinPoint, apiRateLimiter, "Weapon API");
    }

    /**
     * 攔截批量處理相關的API請求
     */
    @Around("execution(* tw.com.tymbackend.module.people.controller.*.batch*(..))")
    public Object rateLimitBatchApis(ProceedingJoinPoint joinPoint) throws Throwable {
        return applyRateLimit(joinPoint, batchApiRateLimiter, "Batch API");
    }

    private Object applyRateLimit(ProceedingJoinPoint joinPoint, Bucket bucket, String apiType) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        
        // Rate Limiter 檢查
        if (!bucket.tryConsume(1)) {
            logger.warn("{} - {}: Rate Limiter 限制，請稍後再試", apiType, methodName);
            throw ResilienceException.rateLimitExceeded();
        }
        
        // 通過 Rate Limiter，執行實際業務邏輯
        logger.debug("{} - {}: 請求通過 Rate Limiter", apiType, methodName);
        return joinPoint.proceed();
    }
}
