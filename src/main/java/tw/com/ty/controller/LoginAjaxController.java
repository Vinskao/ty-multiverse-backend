package tw.com.ty.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;  // Import this class
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping(path = {"/secure/login.controller"})
    public String login(@RequestBody String json) {
        JSONObject resJson = new JSONObject();

        Map<String, String> errorMsgs = new HashMap<>();
		JSONObject jsonList = new JSONObject(json);
        if (jsonList.getString("username") == null || jsonList.getString("username").length() == 0) {
            errorMsgs.put("xxx1", messageSource.getMessage("login.required.id", null, LocaleContextHolder.getLocale()));
        }
        if (jsonList.getString("password") == null || jsonList.getString("password").length() == 0) {
            errorMsgs.put("xxx2", messageSource.getMessage("login.required.pwd", null, LocaleContextHolder.getLocale()));
        }

        // Add error messages to the response JSON
        resJson.put("errors", errorMsgs);

        // If there are errors, return the JSON response with error messages
        if (!errorMsgs.isEmpty()) {
            resJson.put("message", "validation_failed");
            resJson.put("success", false);
            return resJson.toString();
        }

        // 呼叫企業邏輯程式
        CustomerBean bean = customerService.login(jsonList.getString("username"), jsonList.getString("password"));

        // 根據執行結果呼叫View
        if (bean == null) {
            resJson.put("message", "failed");
            resJson.put("success", false);
            return resJson.toString();
        } else {
            resJson.put("message", "success");
            resJson.put("success", true);
            return resJson.toString();
        }
    }
}
