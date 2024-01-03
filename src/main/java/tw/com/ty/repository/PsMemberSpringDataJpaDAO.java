package tw.com.ty.repository;

import java.util.List;

import org.json.JSONObject;

import tw.com.ty.domain.PsMemberBean;
//客製化功能放此
public interface PsMemberSpringDataJpaDAO {
    //spring會自動加上abstract
    public abstract List<PsMemberBean> find(JSONObject param);
    public abstract long count(JSONObject param);

}
