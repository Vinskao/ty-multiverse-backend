package tw.com.ty.dao;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import jakarta.persistence.PersistenceContext;
import tw.com.ty.domain.CustomerBean;

@Repository
public class CustomerDAOHibernate implements CustomerDAO {
	@PersistenceContext
	private Session session;

	public Session getSession() {
		return this.session;
	}
	@Override
	public CustomerBean select(String custid) {
		if(custid!=null) {
			return this.getSession().get(CustomerBean.class, custid);
		}
		return null;
	}
	@Override
	public boolean update(byte[] password, String email, java.util.Date birth, String custid) {
		if(custid!=null) {
			CustomerBean temp = this.getSession().get(CustomerBean.class, custid);
			if(temp!=null) {
				temp.setPassword(password);
				temp.setEmail(email);
				temp.setBirth(birth);
				return true;
			}
		}
		return false;
	}
}
