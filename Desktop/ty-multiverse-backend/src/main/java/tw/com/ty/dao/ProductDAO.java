package tw.com.ty.dao;

import java.util.List;

import org.json.JSONObject;

import tw.com.ty.domain.ProductBean;

public interface ProductDAO {

	ProductBean select(Integer id);

	List<ProductBean> select();

	ProductBean insert(ProductBean bean);

	ProductBean update(ProductBean bean);

	boolean delete(Integer id);

	List<ProductBean> find(JSONObject param);

	long count(JSONObject param);

}