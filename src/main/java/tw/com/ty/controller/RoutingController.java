package tw.com.ty.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class RoutingController {
	@RequestMapping(
			path = {"/", "/index"},
			method = {RequestMethod.GET}
	)
	public String method1() {
		
		return "/index";
	}
	@GetMapping(path = {"/secure/login"})
	public String method2() {
		return "/secure/login";
	}
	@GetMapping(value = {"/pages/product"})
	public String method3() {
		return "/pages/product";
	}
	@GetMapping("/pages/display")
	public String method4() {
		return "/pages/display";
	}
}
//app.route(‘/’,methods=[‘POST’,‘GET’])
// 這一行是告訴 ‘/’允許的method有什麼
// 然後為什麼我明明form只用了POST，為什麼我還加了GET呢？能不能拿掉？答案是不行！
// 為什麼？因為他原本你什麼都沒寫時他預設就是GET，他要GET我們的URL來知道他要去哪一個網站呀～
// 所以如果你把GET拿掉後，他就不知道他現在的URL在哪，就不知道要呈現哪個HTML了。


