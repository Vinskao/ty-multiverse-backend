package tw.com.ty.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tw.com.ty.domain.CustomerBean;

@SpringBootTest
public class CustomerServiceTests {
	@Autowired
	private CustomerService customerService;
	
	@Test
	public void testLogin() {
		CustomerBean login = customerService.login(null, null);
		System.out.println("login="+login);		
	}
	
	@Test
	public void testChangePassword() {
		boolean change = customerService.changePassword("Ellen", "EEE", "E");
		System.out.println("change="+change);
	}
}
