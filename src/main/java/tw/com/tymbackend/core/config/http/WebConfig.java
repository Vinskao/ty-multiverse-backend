package tw.com.tymbackend.core.config.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tw.com.tymbackend.core.config.session.SessionDiagnosticInterceptor;

/**
 * Web MVC 配置類
 * 
 * 負責配置靜態資源映射，包括 JavaDoc 文件的訪問路徑。
 */
@Configuration
@EnableAspectJAutoProxy
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired(required = false)
    private SessionDiagnosticInterceptor sessionDiagnosticInterceptor;

    /**
     * 配置靜態資源處理器
     * 
     * 將 /javadoc/** 路徑映射到 classpath:/static/javadoc/ 目錄，
     * 使得 JavaDoc 文件可以通過 HTTP 訪問。
     * 
     * @param registry 資源處理器註冊表
     */
    @Override
    public void addResourceHandlers(@SuppressWarnings("null") ResourceHandlerRegistry registry) {
        // 配置 JavaDoc 靜態資源映射
        registry.addResourceHandler("/javadoc/**")
                .addResourceLocations("classpath:/static/javadoc/")
                .setCachePeriod(3600) // 設置緩存時間為 1 小時
                .resourceChain(true);
        
        // 也可以配置其他靜態資源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }

    /**
     * 配置CORS跨域請求
     * 
     * @param registry CORS註冊表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    /**
     * 配置攔截器
     * 
     * @param registry 攔截器註冊表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 註冊 Session 診斷攔截器
        if (sessionDiagnosticInterceptor != null) {
            registry.addInterceptor(sessionDiagnosticInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/actuator/**", "/static/**", "/favicon.ico");
        }
    }
}