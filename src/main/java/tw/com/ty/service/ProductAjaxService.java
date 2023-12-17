package tw.com.ty.service;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.ty.dao.ProductDAO;
import tw.com.ty.domain.ProductBean;
import tw.com.ty.util.DatetimeConverter;

@Service
@Transactional(rollbackFor={Exception.class})
public class ProductAjaxService {
	@Autowired
	private ProductDAO productDao = null;
	
	public long count(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			return productDao.count(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public List<ProductBean> find(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			return productDao.find(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ProductBean findById(Integer id) {
		return productDao.select(id);
	}
	public boolean exists(Integer id) {
		return productDao.select(id) != null;
	}

	public ProductBean create(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			Integer id = obj.isNull("id") ? null : obj.getInt("id");
			String name = obj.isNull("name") ? null : obj.getString("name");
			Double price = obj.isNull("price") ? null : obj.getDouble("price");
			String make = obj.isNull("make") ? null : obj.getString("make");
			Integer expire = obj.isNull("expire") ? null : obj.getInt("expire");
			
			ProductBean insert = new ProductBean();
			insert.setId(id);
			insert.setName(name);
			insert.setPrice(price);
			insert.setMake(null);
			insert.setMake(DatetimeConverter.parse(make, "yyyy-MM-dd"));
			insert.setExpire(expire);
			
			return productDao.insert(insert);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public ProductBean modify(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			Integer id = obj.isNull("id") ? null : obj.getInt("id");
			String name = obj.isNull("name") ? null : obj.getString("name");
			Double price = obj.isNull("price") ? null : obj.getDouble("price");
			String make = obj.isNull("make") ? null : obj.getString("make");
			Integer expire = obj.isNull("expire") ? null : obj.getInt("expire");

			ProductBean update = productDao.select(id);
			update.setName(name);
			update.setPrice(price);
			update.setMake(null);
			update.setMake(DatetimeConverter.parse(make, "yyyy-MM-dd"));
			update.setExpire(expire);
			
			return productDao.update(update);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public boolean remove(Integer id) {
		try {
			return productDao.delete(id);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return false;
	}
}
