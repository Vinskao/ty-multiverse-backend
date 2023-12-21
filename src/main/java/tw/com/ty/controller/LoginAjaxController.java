package tw.com.ty.controller;

import java.util.HashMap;
import java.util.Locale;
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
    public String login(@RequestBody String data, Locale locale) {
        JSONObject resJson = new JSONObject();
		JSONObject jsonList = new JSONObject(data);

        String username = jsonList.isNull("username") ? null : jsonList.getString("username");
        String password = jsonList.isNull("password") ? null : jsonList.getString("password");

        if ((username == null && password == null)) {
            resJson.put("message", "帳號和密碼都是必填的");
            resJson.put("success", false);
                return resJson.toString();
        }
        if (username == null || username.length() == 0) {
            resJson.put("message", messageSource.getMessage("login.required.id", null,  locale));
            resJson.put("sucess", false);
                return resJson.toString();

        }
        if (password == null || password.length() == 0) {
            resJson.put("message", messageSource.getMessage("login.required.pwd", null,  locale));
            resJson.put("sucess", false);
                return resJson.toString();
        } 
        
        else {
            // 呼叫企業邏輯程式
            CustomerBean bean = customerService.login(username, password);

            // 根據執行結果呼叫View
            if (bean == null) {
                resJson.put("message", "無此帳密");
                resJson.put("success", false);

            } else {
                resJson.put("message", "成功登入");
                resJson.put("success", true);

            }
        }
        
    
        return resJson.toString();
    }
}
