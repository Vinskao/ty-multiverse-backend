package tw.com.tymbackend.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * JavaDoc 控制器
 * 
 * 提供 JavaDoc 文件的訪問入口和重定向功能。
 */
@Controller
@RequestMapping("/docs")
public class JavaDocController {

    /**
     * JavaDoc 首頁重定向
     * 
     * 將 /docs 請求重定向到 JavaDoc 的主頁面，方便用戶訪問。
     * 
     * @return 重定向到 JavaDoc 首頁的響應
     */
    @GetMapping
    public String redirectToJavaDoc() {
        return "redirect:/javadoc/index.html";
    }

    /**
     * JavaDoc API 文件重定向
     * 
     * 提供 /docs/api 的便捷訪問路徑。
     * 
     * @return 重定向到 JavaDoc API 文件的響應
     */
    @GetMapping("/api")
    public String redirectToApiDoc() {
        return "redirect:/javadoc/index.html";
    }
}