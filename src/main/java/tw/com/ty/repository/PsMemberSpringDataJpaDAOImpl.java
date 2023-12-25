// package tw.com.ty.repository;

// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.util.ArrayList;
// import java.util.List;

// import org.hibernate.Session;
// import org.json.JSONObject;

// import jakarta.persistence.PersistenceContext;
// import jakarta.persistence.TypedQuery;
// import jakarta.persistence.criteria.CriteriaBuilder;
// import jakarta.persistence.criteria.CriteriaQuery;
// import jakarta.persistence.criteria.Predicate;
// import jakarta.persistence.criteria.Root;
// import tw.com.ty.domain.ProductBean;
// import tw.com.ty.util.DatetimeConverter;
// //interface名稱＋Impl
// //客製化功能放此
// public class PsMemberSpringDataJpaDAOImpl implements PsMemberSpringDataJpaDAO {
	
//     @PersistenceContext
// 	private Session session;
// 	public Session getSession() {
// 		return this.session;
// 	}
//     @Override
//     public long count(JSONObject param) {
// //		select count(*) from product where id=? and name like ? and price < ? and make > ? and expire >= ?
// 		Integer id = param.isNull("id") ? null : param.getInt("id");
// 		String name = param.isNull("name") ? null : param.getString("name");
// 		Double price = param.isNull("price") ? null : param.getDouble("price");
// 		String temp = param.isNull("make") ? null : param.getString("make");
// 		Integer expire = param.isNull("expire") ? null : param.getInt("expire");

// 		CriteriaBuilder builder = this.getSession().getCriteriaBuilder();
// 		CriteriaQuery<Long> query = builder.createQuery(Long.class);

// //		from product
// 		Root<ProductBean> table = query.from(ProductBean.class);

// //		select count(*)
// 		query = query.select(builder.count(table));
		
// 		List<Predicate> predicates = new ArrayList<>();
// //		id=?
// 		if(id!=null) {
// 			predicates.add(builder.equal(table.get("id"), id));
// 		}
		
// //		name like ?
// 		if(name!=null && name.length()!=0) {
// 			predicates.add(builder.like(table.get("name"), "%"+name+"%"));
// 		}
		
// //		price < ?
// 		if(price!=null) {
// 			predicates.add(builder.lessThan(table.get("price"), price));
// 		}
		
// //		make > ?
// 		if(temp!=null && temp.length()!=0) {
// 			java.util.Date make = DatetimeConverter.parse(temp, "yyyy-MM-dd");
// 			predicates.add(builder.greaterThan(table.get("make"), make));
// 		}
		
// //		expire >= ?
// 		if(expire!=null) {
// 			predicates.add(builder.greaterThanOrEqualTo(table.get("expire"), expire));
// 		}
		
// 		if(predicates!=null && !predicates.isEmpty()) {
// 			query = query.where(predicates.toArray(new Predicate[0]));			
// 		}
		
// 		TypedQuery<Long> typedQuery = this.getSession().createQuery(query);
// 		Long result = typedQuery.getSingleResult();
// 		if(result!=null) {
// 			return result;
// 		} else {
// 			return 0;
// 		}
// 	}
//     @Override
// 	public List<ProductBean> find(JSONObject param) {
// //		select * from product where id=? and name like ? and price < ? and make > ? and expire >= ? order by id desc
// 		Integer id = param.isNull("id") ? null : param.getInt("id");
// 		String name = param.isNull("name") ? null : param.getString("name");
// 		Double price = param.isNull("price") ? null : param.getDouble("price");
// 		String temp = param.isNull("make") ? null : param.getString("make");
// 		Integer expire = param.isNull("expire") ? null : param.getInt("expire");

// 		int start = param.isNull("start") ? 0 : param.getInt("start");
// 		int rows = param.isNull("rows") ? 0 : param.getInt("rows");
		
// 		CriteriaBuilder builder = this.getSession().getCriteriaBuilder();
// 		CriteriaQuery<ProductBean> query = builder.createQuery(ProductBean.class);

// //		from product
// 		Root<ProductBean> table = query.from(ProductBean.class);

// 		List<Predicate> predicates = new ArrayList<>();
// //		id=?
// 		if(id!=null) {
// 			predicates.add(builder.equal(table.get("id"), id));
// 		}
		
// //		name like ?
// 		if(name!=null && name.length()!=0) {
// 			predicates.add(builder.like(table.get("name"), "%"+name+"%"));
// 		}
		
// //		price < ?
// 		if(price!=null) {
// 			predicates.add(builder.lessThan(table.get("price"), price));
// 		}
		
// //		make > ?
// 		if(temp!=null && temp.length()!=0) {
// 			java.util.Date make = DatetimeConverter.parse(temp, "yyyy-MM-dd");
// 			predicates.add(builder.greaterThan(table.get("make"), make));
// 		}
		
// //		expire >= ?
// 		if(expire!=null) {
// 			predicates.add(builder.greaterThanOrEqualTo(table.get("expire"), expire));
// 		}
		
// 		if(predicates!=null && !predicates.isEmpty()) {
// 			query = query.where(predicates.toArray(new Predicate[0]));			
// 		}

// //		order by id desc
// 		query = query.orderBy(builder.desc(table.get("id")));
		
// 		TypedQuery<ProductBean> typedQuery = this.getSession().createQuery(query)
// 				.setFirstResult(start)
// 				.setMaxResults(rows);
// 		List<ProductBean> result = typedQuery.getResultList();
// 		if(result!=null && !result.isEmpty()) {
// 			return result;
// 		} else {
// 			return null;
// 		}
// 	}
	


// 	@Override
// 	public List<Member> select() {
// 		List<Member> result = null;
// 		try (Connection conn = dataSource.getConnection();
// 				PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
// 				ResultSet rset = stmt.executeQuery();) {

// 			result = new ArrayList<>();
// 			while (rset.next()) {
// 				Member row = new Member();
// 				row.setId(rset.getInt("id"));
// 				row.setName(rset.getString("name"));
// 				row.setPhysicPower(rset.getInt("physic_power"));
// 				row.setMagicPower(rset.getInt("magic_power"));
// 				row.setUtilityPower(rset.getInt("utility_power"));
// 				row.setArmyName(rset.getString("army_name"));

// 				result.add(row);
// 			}
// 		} catch (SQLException e) {
// 			e.printStackTrace();
// 		}
// 		return result;

// 	}

// //	@Override
// 	public List<Member> find(JSONObject param) {
// 		// 假設你的 JSON 對象中有一個 "id" 鍵（key），你可以使用 getInt 方法獲取該鍵對應的整數值
// //		select * from member where id=? and name like ? and price < ? and make > ? and expire >= ? order by id desc
// 		// param.isNull("id") 的結果是 false，表示 "id" 參數不為空，那麼就執行 param.getInt("id") 取得 "id"
// 		// 參數的整數值。
// 		Integer id = param.isNull("id") ? null : param.getInt("id");
// 		String nameOriginal = param.isNull("nameOriginal") ? null : param.getString("nameOriginal");
// 		String codeName = param.isNull("codeName") ? null : param.getString("codeName");
// 		String name = param.isNull("name") ? null : param.getString("name");
// 		Integer physicPower = param.isNull("physicPower") ? null : param.getInt("physicPower");
// 		Integer magicPower = param.isNull("magicPower") ? null : param.getInt("magicPower");
// 		Integer utilityPower = param.isNull("utilityPower") ? null : param.getInt("utilityPower");
// 		String temp = param.isNull("dob") ? null : param.getString("dob");
// 		String race = param.isNull("race") ? null : param.getString("race");
// 		String attributes = param.isNull("attributes") ? null : param.getString("attributes");
// 		char gender = param.isNull("gender") ? '\0' : param.getString("gender").charAt(0);
// 		String assSize = param.isNull("assSize") ? null : param.getString("assSize");
// 		String boobsSize = param.isNull("boobsSize") ? null : param.getString("boobsSize");
// 		Integer heightCm = param.isNull("heightCm") ? null : param.getInt("heightCm");
// 		Integer weightKg = param.isNull("weightKg") ? null : param.getInt("weightKg");
// 		String army = param.isNull("army") ? null : param.getString("army");
// 		String dept = param.isNull("dept") ? null : param.getString("dept");
// 		String profession = param.isNull("profession") ? null : param.getString("profession");
// 		String combat = param.isNull("combat") ? null : param.getString("combat");
// 		String favoriteFoods = param.isNull("favoriteFoods") ? null : param.getString("favoriteFoods");
// 		String job = param.isNull("job") ? null : param.getString("job");
// 		String physics = param.isNull("physics") ? null : param.getString("physics");
// 		String knownAs = param.isNull("knownAs") ? null : param.getString("knownAs");
// 		String email = param.isNull("E-mail") ? null : param.getString("E-mail");
// 		String personally = param.isNull("Personally") ? null : param.getString("Personally");
// 		String mainWeapon = param.isNull("mainWeapon") ? null : param.getString("mainWeapon");
// 		String subWeapon = param.isNull("subWeapon") ? null : param.getString("subWeapon");
// 		String interest = param.isNull("interest") ? null : param.getString("interest");
// 		String likes = param.isNull("likes") ? null : param.getString("likes");
// 		String dislikes = param.isNull("dislikes") ? null : param.getString("dislikes");
// 		String faction = param.isNull("faction") ? null : param.getString("faction");
// 		Integer armyId = param.isNull("armyId") ? null : param.getInt("armyId");
// 		Integer deptId = param.isNull("deptId") ? null : param.getInt("deptId");

// 		int start = param.isNull("start") ? 0 : param.getInt("start");
// 		int row = param.isNull("row") ? 0 : param.getInt("row");

// 		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
// 		// 獲取了 Hibernate Session 對象的 CriteriaBuilder。CriteriaBuilder 是用於構建 Criteria
// 		// Query 的核心介面。CriteriaQuery 是 JPA 中用於定義查詢的 API，它允許你以更為面向對象的方式構建查詢
// 		CriteriaQuery<Member> query = builder.createQuery(Member.class);
// //		from
// 		// Root 是 Criteria Query 中的一個介面，表示查詢的根實體（Root Entity）。<member> 是泛型參數
// 		Root<Member> table = query.from(Member.class);
// 		List<Predicate> predicates = new ArrayList<>();
// 		// ArrayList 和 List 之間的區別實際上是 ArrayList 是 List 的一種實現。List 是 Java
// 		// 集合框架的一部分，它是一個介面，而 ArrayList 是 List 接口的一種具體實現
// 		// 使用 ArrayList 的同時，你還是可以使用 List 的方法。
// 		// List 是一個接口，它定義了一個有序的集合，可以包含重複的元素
// 		// 實體（Entity）**通常指的是資料庫中的一個表格，表格中的每一行都代表系統中的一個實體。這種映射使得程式碼中的物件（Java
// 		// 中的類實例）與資料庫中的表格行之間建立了一對一或者一對多的對應關係
// 		// "根" 就是指查詢的起點
// //		=?
// 		if (param.has("id") && id!=null) {
// 			// isNull and getString: The param.isNull("paramName") method returns a boolean
// 			// indicating whether the specified key is present and the associated value is
// 			// null. It does not check if the key is not present in the JSON. Therefore,
// 			// using param.isNull("paramName") for keys that might not be present in the
// 			// JSON could lead to incorrect logic.
// 			// It's better to use param.has("paramName") to check if a key is present
// 			predicates.add(builder.equal(table.get("id"), id));
// 		}
// //		=?
// 		if (codeName != null && codeName.length() != 0) {
// 			predicates.add(builder.like(table.get("codeName"), "%" + codeName + "%"));
// 		}
// //		dob < ?		
// 		if (temp != null && temp.length() != 0) {
// 			Date dob = DatetimeConverter.parse(temp, "yyyy-MM-dd");
// 			predicates.add(builder.lessThan(table.get("dob"), dob));
// 		}
// //		>= ?
// 		if (physicPower != null) {
// 			predicates.add(builder.greaterThanOrEqualTo(table.get("physicPower"), physicPower));
// 		}
// //		>= ?
// 		if (magicPower != null) {
// 			predicates.add(builder.greaterThanOrEqualTo(table.get("magicPower"), magicPower));
// 		}
// //		>= ?
// 		if (utilityPower != null) {
// 			predicates.add(builder.greaterThanOrEqualTo(table.get("utilityPower"), utilityPower));
// 		}
// //		where
// 		if (predicates != null && !predicates.isEmpty()) {
// 			query = query.where(predicates.toArray(new Predicate[0]));
// 		}

// 		TypedQuery<Member> typedQuery = entityManager.createQuery(query).setFirstResult(start).setMaxResults(row);
// 		List<Member> result = typedQuery.getResultList();
// 		if (result != null && !result.isEmpty()) {
// 			return result;
// 		} else {
// 			return null;
// 		}
// 	}
// }
