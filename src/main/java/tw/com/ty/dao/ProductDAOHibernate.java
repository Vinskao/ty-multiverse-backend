package tw.com.ty.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import tw.com.ty.domain.ProductBean;
import tw.com.ty.util.DatetimeConverter;

@Repository
public class ProductDAOHibernate implements ProductDAO {
	@PersistenceContext
	private Session session;
	public Session getSession() {
		return this.session;
	}

	@Override
	public long count(JSONObject param) {
//		select count(*) from product where id=? and name like ? and price < ? and make > ? and expire >= ?
		Integer id = param.isNull("id") ? null : param.getInt("id");
		String name = param.isNull("name") ? null : param.getString("name");
		Double price = param.isNull("price") ? null : param.getDouble("price");
		String temp = param.isNull("make") ? null : param.getString("make");
		Integer expire = param.isNull("expire") ? null : param.getInt("expire");

		CriteriaBuilder builder = this.getSession().getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);

//		from product
		Root<ProductBean> table = query.from(ProductBean.class);

//		select count(*)
		query = query.select(builder.count(table));
		
		List<Predicate> predicates = new ArrayList<>();
//		id=?
		if(id!=null) {
			predicates.add(builder.equal(table.get("id"), id));
		}
		
//		name like ?
		if(name!=null && name.length()!=0) {
			predicates.add(builder.like(table.get("name"), "%"+name+"%"));
		}
		
//		price < ?
		if(price!=null) {
			predicates.add(builder.lessThan(table.get("price"), price));
		}
		
//		make > ?
		if(temp!=null && temp.length()!=0) {
			java.util.Date make = DatetimeConverter.parse(temp, "yyyy-MM-dd");
			predicates.add(builder.greaterThan(table.get("make"), make));
		}
		
//		expire >= ?
		if(expire!=null) {
			predicates.add(builder.greaterThanOrEqualTo(table.get("expire"), expire));
		}
		
		if(predicates!=null && !predicates.isEmpty()) {
			query = query.where(predicates.toArray(new Predicate[0]));			
		}
		
		TypedQuery<Long> typedQuery = this.getSession().createQuery(query);
		Long result = typedQuery.getSingleResult();
		if(result!=null) {
			return result;
		} else {
			return 0;
		}
	}
	
	@Override
	public List<ProductBean> find(JSONObject param) {
//		select * from product where id=? and name like ? and price < ? and make > ? and expire >= ? order by id desc
		Integer id = param.isNull("id") ? null : param.getInt("id");
		String name = param.isNull("name") ? null : param.getString("name");
		Double price = param.isNull("price") ? null : param.getDouble("price");
		String temp = param.isNull("make") ? null : param.getString("make");
		Integer expire = param.isNull("expire") ? null : param.getInt("expire");

		int start = param.isNull("start") ? 0 : param.getInt("start");
		int rows = param.isNull("rows") ? 0 : param.getInt("rows");
		
		CriteriaBuilder builder = this.getSession().getCriteriaBuilder();
		CriteriaQuery<ProductBean> query = builder.createQuery(ProductBean.class);

//		from product
		Root<ProductBean> table = query.from(ProductBean.class);

		List<Predicate> predicates = new ArrayList<>();
//		id=?
		if(id!=null) {
			predicates.add(builder.equal(table.get("id"), id));
		}
		
//		name like ?
		if(name!=null && name.length()!=0) {
			predicates.add(builder.like(table.get("name"), "%"+name+"%"));
		}
		
//		price < ?
		if(price!=null) {
			predicates.add(builder.lessThan(table.get("price"), price));
		}
		
//		make > ?
		if(temp!=null && temp.length()!=0) {
			java.util.Date make = DatetimeConverter.parse(temp, "yyyy-MM-dd");
			predicates.add(builder.greaterThan(table.get("make"), make));
		}
		
//		expire >= ?
		if(expire!=null) {
			predicates.add(builder.greaterThanOrEqualTo(table.get("expire"), expire));
		}
		
		if(predicates!=null && !predicates.isEmpty()) {
			query = query.where(predicates.toArray(new Predicate[0]));			
		}

//		order by id desc
		query = query.orderBy(builder.desc(table.get("id")));
		
		TypedQuery<ProductBean> typedQuery = this.getSession().createQuery(query)
				.setFirstResult(start)
				.setMaxResults(rows);
		List<ProductBean> result = typedQuery.getResultList();
		if(result!=null && !result.isEmpty()) {
			return result;
		} else {
			return null;
		}
	}
	
	@Override
	public ProductBean select(Integer id) {
		if(id!=null) {
			return this.getSession().get(ProductBean.class, id);
		}
		return null;
	}
	
	@Override
	public List<ProductBean> select() {
		return this.getSession().createQuery("from ProductBean", ProductBean.class).list();
	}
	
	@Override
	public ProductBean insert(ProductBean bean) {
		if(bean!=null && bean.getId()!=null) {
			ProductBean temp = this.getSession().get(ProductBean.class, bean.getId());
			if(temp==null) {
				this.getSession().persist(bean);
				return bean;
			}
		}
		return null;
	}
	@Override
	public ProductBean update(ProductBean bean) {
		if(bean!=null && bean.getId()!=null) {
			ProductBean temp = this.getSession().get(ProductBean.class, bean.getId());
			if(temp!=null) {
				return (ProductBean) this.getSession().merge(bean);
			}
		}
		return null;
	}
	@Override
	public boolean delete(Integer id) {
		if(id!=null) {
			ProductBean temp = this.getSession().get(ProductBean.class, id);
			if(temp!=null) {
				this.getSession().remove(temp);
				return true;
			}
		}
		return false;
	}
}
