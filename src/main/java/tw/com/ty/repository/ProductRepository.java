package tw.com.ty.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.ty.domain.ProductBean;
//繼承有方法的東西
public interface ProductRepository extends JpaRepository<ProductBean,Integer>, ProductSpringDataJpaDAO {
    //pk是integer
    //已具備基本curd
    //現在去定義一個介面再回來加在extends後

}
