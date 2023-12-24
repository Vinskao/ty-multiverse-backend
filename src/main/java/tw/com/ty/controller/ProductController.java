// package tw.com.ty.controller;

// import java.text.SimpleDateFormat;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Locale;
// import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.propertyeditors.CustomDateEditor;
// import org.springframework.context.MessageSource;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.validation.BindingResult;
// import org.springframework.web.bind.WebDataBinder;
// import org.springframework.web.bind.annotation.InitBinder;
// import org.springframework.web.bind.annotation.PostMapping;

// import tw.com.ty.domain.ProductBean;
// import tw.com.ty.service.ProductService;

// @Controller
// public class ProductController {
// 	@Autowired
// 	private ProductService productService;
	
// 	@Autowired
// 	private MessageSource messageSource;
	
// 	@InitBinder
// 	public void initBinder(WebDataBinder webDataBinder) {
// 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
// 		CustomDateEditor editor = new CustomDateEditor(sdf, true);
// 		webDataBinder.registerCustomEditor(java.util.Date.class, editor);
// 	}
	
// 	@PostMapping(path = {"/pages/product.controller"})
// 	public String handlerMethod(Locale locale, Model model,
// 			String prodaction, ProductBean bean, BindingResult bindingResult) {
// //接收使用者輸入的資料
// //驗證使用者輸入的資料
// 		Map<String, String> errors = new HashMap<>();
// 		model.addAttribute("errors", errors);
		
// 		if("Insert".equals(prodaction) || "Update".equals(prodaction) || "Delete".equals(prodaction)) {
// 			if(bean==null || bean.getId()==null) {
// 				errors.put("id", messageSource.getMessage("product.required.id", new String[] {prodaction}, locale));
// 			}
// 		}
		
// //轉換使用者輸入的資料
// 		if(bindingResult!=null && bindingResult.hasFieldErrors("id")) {
// 			errors.put("id", messageSource.getMessage("product.format.id", null, locale));
// 		}
// 		if(bindingResult!=null && bindingResult.hasFieldErrors("price")) {
// 			errors.put("price", messageSource.getMessage("product.format.price", null, locale));
// 		}
// 		if(bindingResult!=null && bindingResult.hasFieldErrors("make")) {
// 			errors.put("make", messageSource.getMessage("product.format.make", null, locale));
// 		}
// 		if(bindingResult!=null && bindingResult.hasFieldErrors("expire")) {
// 			errors.put("expire", messageSource.getMessage("product.format.expire", null, locale));
// 		}
		
// 		if(errors!=null && !errors.isEmpty()) {
// 			return "/pages/product";
// 		}
		
// //呼叫企業邏輯程式
// //根據執行結果呼叫View
// 		if("Select".equals(prodaction)) {
// 			List<ProductBean> result = productService.select(bean);
// 			model.addAttribute("select", result);
// 			return "/pages/display";
// 		} else if("Insert".equals(prodaction)) {
// 			ProductBean result = productService.insert(bean);
// 			if(result!=null) {
// 				model.addAttribute("insert", result);
// 			} else {
// 				errors.put("action", messageSource.getMessage("product.failed.insert", null, locale));				
// 			}
// 			return "/pages/product";
// 		} else if("Update".equals(prodaction)) {
// 			ProductBean result = productService.update(bean);
// 			if(result!=null) {
// 				model.addAttribute("update", result);
// 			} else {
// 				errors.put("action", messageSource.getMessage("product.failed.update", null, locale));
// 			}
// 			return "/pages/product";
// 		} else if("Delete".equals(prodaction)) {
// 			boolean result = productService.delete(bean);
// 			if(result) {
// 				model.addAttribute("delete", 1);
// 			} else {
// 				model.addAttribute("delete", 0);
// 			}
// 			return "/pages/product";
// 		} else {
// 			errors.put("action", messageSource.getMessage("product.failed.unknown", new String[] {prodaction}, locale));
// 			return "/pages/product";
// 		}
// 	}
// }
