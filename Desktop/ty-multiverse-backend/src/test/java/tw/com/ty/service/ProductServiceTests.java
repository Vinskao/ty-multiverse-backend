package tw.com.ty.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tw.com.ty.domain.ProductBean;

@SpringBootTest
public class ProductServiceTests {
	@Autowired
	private ProductService productService;
	
	@Test
	public void testSelect() {
		List<ProductBean> selects = productService.select(null);
		System.out.println("selects="+selects);
	}
	
	@Test
	public void insert() {
		ProductBean insert = new ProductBean();
		insert.setId(102);
		insert.setName("hahaha");
		insert.setPrice(1.23);
		insert.setMake(new java.util.Date());
		insert.setExpire(45);
		
		ProductBean result = productService.insert(insert);
		System.out.println("result="+result);
	}
}
