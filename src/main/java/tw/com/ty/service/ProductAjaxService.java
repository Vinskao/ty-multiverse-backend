package tw.com.ty.service;

import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.ty.domain.ProductBean;
import tw.com.ty.repository.ProductRepository;
import tw.com.ty.util.DatetimeConverter;

@Service
@Transactional(rollbackFor={Exception.class})
public class ProductAjaxService {
	@Autowired
	private ProductRepository productRepository = null;
	
	public long count(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			return productRepository.count(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public List<ProductBean> find(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			return productRepository.find(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ProductBean findById(Integer id) {
		if(id!=null) {
			Optional<ProductBean> optional = productRepository.findById(id);
			if(optional.isPresent()) {
				return optional.get();
			}
		}
		return null;
	}
	public boolean exists(Integer id) {
		if(id!=null) {
			return productRepository.existsById(id);
		}
		return false;
	}

	public ProductBean create(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			Integer id = obj.isNull("id") ? null : obj.getInt("id");
			String name = obj.isNull("name") ? null : obj.getString("name");
			Double price = obj.isNull("price") ? null : obj.getDouble("price");
			String make = obj.isNull("make") ? null : obj.getString("make");
			Integer expire = obj.isNull("expire") ? null : obj.getInt("expire");
			
			if(id != null && !productRepository.existsById(id) ) {
				ProductBean insert = new ProductBean();
				insert.setId(id);
				insert.setName(name);
				insert.setPrice(price);
				insert.setMake(null);
				insert.setMake(DatetimeConverter.parse(make, "yyyy-MM-dd"));
				insert.setExpire(expire);
				
				return productRepository.save(insert);
			}
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

			if(id!=null && productRepository.existsById(id) ){
				Optional<ProductBean> optional = productRepository.findById(id);
				if(optional.isPresent()){
					ProductBean update = optional.get();
					update.setName(name);
					update.setPrice(price);
					update.setMake(null);
					update.setMake(DatetimeConverter.parse(make, "yyyy-MM-dd"));
					update.setExpire(expire);
				return productRepository.save(update);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public boolean remove(Integer id) {
		if(id!=null) {
			} try {
				if(productRepository.existsById(id)){
				productRepository.deleteById(id);
				return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		return false;
	}
}
