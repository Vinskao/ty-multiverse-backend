package tw.com.tymbackend.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * 配置 RestTemplate 以信任所有證書
 * author: wavo
 */
@Configuration
public class RestTemplateConfig {

    // 創建信任所有證書的 TrustManager
    @Bean
    RestTemplate restTemplate() throws Exception {
        // 創建信任所有證書的 TrustManager
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                // 返回所有可接受的證書
                public X509Certificate[] getAcceptedIssuers() { return null; }
                // 檢查客戶端證書
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                // 檢查伺服器證書
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };

        // 創建忽略主機名驗證的 HostnameVerifier
        HostnameVerifier allHostsValid = (hostname, session) -> true;

        // 設置 SSL 上下文
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // 獲取 HttpsURLConnection 的默認 SSLSocketFactory
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        
        // 設置默認主機名驗證器
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        // 設置連接和讀取超時
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // 設置連接超時時間為 10 秒
        requestFactory.setConnectTimeout(10000);
        // 設置讀取超時時間為 10 秒
        requestFactory.setReadTimeout(10000);

        // 創建 RestTemplate 實例
        return new RestTemplate(requestFactory);
    }       
}
