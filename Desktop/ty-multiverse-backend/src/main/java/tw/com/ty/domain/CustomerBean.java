package tw.com.ty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer")
public class CustomerBean {
	@Id
	@Column(name = "custid")
	private String custid;
	
	@Column(name = "password")
	private byte[] password;
	
	@Column(name = "email", columnDefinition = "nvarchar")
	private String email;
	
	@Column(name = "birth")
	private java.util.Date birth;
	@Override
	public String toString() {
		return "CustomerBean [custid=" + custid + ", email=" + email + ", birth=" + birth + "]";
	}
	public String getCustid() {
		return custid;
	}
	public void setCustid(String custid) {
		this.custid = custid;
	}
	public byte[] getPassword() {
		return password;
	}
	public void setPassword(byte[] password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public java.util.Date getBirth() {
		return birth;
	}
	public void setBirth(java.util.Date birth) {
		this.birth = birth;
	}
}
