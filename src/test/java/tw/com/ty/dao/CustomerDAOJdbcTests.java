package tw.com.ty.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tw.com.ty.domain.CustomerBean;

@SpringBootTest
public class CustomerDAOJdbcTests {
	@Autowired
	private CustomerDAO customerDao;
	
	@Test
	public void testSelect() {
		CustomerBean select = customerDao.select("Alex");
		System.out.println("select="+select);
	}

	@Test
	public void testUpdate() {
		boolean update = customerDao.update(
				"EEE".getBytes(), "cma@test", new java.util.Date(0), "Ellen");
		System.out.println("update="+update);
	}
	
}
