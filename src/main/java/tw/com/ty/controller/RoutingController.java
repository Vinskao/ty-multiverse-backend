package tw.com.ty.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class RoutingController {
	@RequestMapping(
			path = {"/", "/index"},
			method = {RequestMethod.POST}
	)
	public String method1() {
		return "/index";
	}
	@PostMapping(path = {"/secure/login"})
	public String method2() {
		return "/secure/login";
	}
	@PostMapping(value = {"/pages/product"})
	public String method3() {
		return "/pages/product";
	}
	@PostMapping("/pages/display")
	public String method4() {
		return "/pages/display";
	}
}
