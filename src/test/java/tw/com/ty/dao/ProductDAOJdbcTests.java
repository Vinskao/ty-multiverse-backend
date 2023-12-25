// package tw.com.ty.dao;

// import java.util.List;

// import org.json.JSONObject;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import jakarta.transaction.Transactional;
// import tw.com.ty.domain.ProductBean;

// @SpringBootTest
// @Transactional
// public class ProductDAOJdbcTests {
	
// 	@Autowired
// 	private ProductDAO productDao;
	
// 	@Test
// 	public void testInsert() {
// 		ProductBean insert = new ProductBean();
// 		insert.setId(101);
// 		insert.setName("hahaha");
// 		insert.setPrice(1.23);
// 		insert.setMake(new java.util.Date());
// 		insert.setExpire(45);
		
// 		ProductBean result = productDao.insert(insert);
// 		System.out.println("result="+result);
// 	}
	
// 	@Test
// 	public void testUpdate() {
// 		ProductBean update = new ProductBean();
// 		update.setId(100);
// 		update.setName("hehehe");
// 		update.setPrice(67.8);
// 		update.setMake(new java.util.Date(1));
// 		update.setExpire(90);
// 		productDao.update(update);
// 	}

// 	@Test
// 	public void testDelete() {
// 		boolean delete = productDao.delete(100);
// 		System.out.println("delete="+delete);
// 	}

// 	@Test
// 	public void testFind() {
// 		JSONObject param = new JSONObject()
// //				.put("id", 100)
// //				.put("name", "a")
// //				.put("price", 12.34)
// //				.put("make", "2023-11-30")
// //				.put("expire", 56)
// 				.put("start", 1)
// 				.put("rows", 20);
		
// 		List<ProductBean> find = productDao.find(param);
// 		System.out.println("find="+find);
		
// 		long count = productDao.count(param);
// 		System.out.println("count="+count);
// 	}
	
// 	@Test
// 	public void testSelect() {
// 		ProductBean select1 = productDao.select(1);
// 		System.out.println("select1="+select1);
		
// 		ProductBean select0 = productDao.select(null);
// 		System.out.println("select0="+select0);
// 	}
// }
