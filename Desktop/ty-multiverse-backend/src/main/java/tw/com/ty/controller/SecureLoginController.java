package tw.com.ty.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpSession;
import tw.com.ty.domain.CustomerBean;
import tw.com.ty.service.CustomerService;

@Controller
@CrossOrigin(origins = "http://localhost:3000")
public class SecureLoginController {
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private CustomerService customerService;

	@PostMapping(path = { "/secure/SecureLogin.controller" }, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> handleLogin(Locale locale, @RequestBody Map<String, String> loginData, // 使用
			HttpSession session) {

		Map<String, Object> response = new HashMap<>();

		// 从 loginData 中获取用户名和密码
		String username = loginData.get("username");
		String password = loginData.get("password");

		// 验证用户输入的数据
		Map<String, String> errorMsgs = new HashMap<>();

		if (username == null || username.length() == 0) {
			errorMsgs.put("xxx1", messageSource.getMessage("login.required.id", null, locale));
		}
		if (password == null || password.length() == 0) {
			errorMsgs.put("xxx2", messageSource.getMessage("login.required.pwd", null, locale));
		}

		if (!errorMsgs.isEmpty()) {
			response.put("success", false);
			response.put("errors", errorMsgs);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		// 调用企业逻辑程序
		CustomerBean bean = customerService.login(username, password);

		// 根据执行结果返回结果
		if (bean == null) {
			errorMsgs.put("xxx2", messageSource.getMessage("login.failed", null, locale));
			response.put("success", false);
			response.put("errors", errorMsgs);
			System.out.println(1);
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		} else {
			session.setAttribute("user", bean);
			response.put("success", true);
			response.put("redirect", "http://localhost:3000/product/"); 
			System.out.println(2);
			return ResponseEntity.ok(response);
		}
	}
}
