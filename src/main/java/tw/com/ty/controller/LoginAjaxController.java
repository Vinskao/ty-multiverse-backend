package tw.com.ty.controller;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder; 
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ty.domain.CustomerBean;
import tw.com.ty.service.CustomerService;

@RestController
@CrossOrigin
@RequestMapping("/secure/login")
public class LoginAjaxController {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private CustomerService customerService;

    @PostMapping(path = {"/ajex.controller"})
    public String login(@RequestBody String data) {
        JSONObject resJson = new JSONObject();
		JSONObject jsonList = new JSONObject(data);

        Map<String, String> errorMsgs = new HashMap<>();
        if (jsonList.getString("username") == null || jsonList.getString("username").length() == 0) {
            errorMsgs.put("username", "請輸入帳號");
        }
        if (jsonList.getString("password") == null || jsonList.getString("password").length() == 0) {
            errorMsgs.put("password", "請輸入密碼");
        }
        resJson.put("errors", errorMsgs);

        if (!errorMsgs.isEmpty()) {
            resJson.put("message", "validation_failed");
            resJson.put("success", false);
            return resJson.toString();
        }

        // 呼叫企業邏輯程式
        CustomerBean bean = customerService.login(jsonList.getString("username"), jsonList.getString("password"));

        // 根據執行結果呼叫View
        if (bean == null) {
            resJson.put("message", "無此帳密");
            resJson.put("success", false);
            return resJson.toString();
        } else {
            resJson.put("message", "成功登入");
            resJson.put("success", true);
            return resJson.toString();
        }
    }
}
