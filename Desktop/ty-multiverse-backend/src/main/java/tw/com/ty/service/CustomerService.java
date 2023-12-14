package tw.com.ty.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.ty.dao.CustomerDAO;
import tw.com.ty.domain.CustomerBean;

@Service
@Transactional
public class CustomerService {
	@Autowired
	private CustomerDAO customerDao;

	public boolean changePassword(String username, String oldPassword, String newPassword) {
		CustomerBean login = this.login(username, oldPassword);
		if(login!=null) {
			if(newPassword!=null && newPassword.length()!=0) {
				byte[] pass = newPassword.getBytes();
				return customerDao.update(
						pass, login.getEmail(), login.getBirth(), username);
			}
		}
		return false;
	}
	
	public CustomerBean login(String username, String password) {
		CustomerBean select = customerDao.select(username);
		if(select!=null) {
			if(password!=null) {
				byte[] pass = select.getPassword();		//資料庫取出
				byte[] temp = password.getBytes();		//使用者輸入
				if(Arrays.equals(pass, temp)) {
					return select;
				}
			}
		}
		return null;
	}
}
