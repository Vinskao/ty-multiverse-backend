package tw.com.ty.controller;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import tw.com.ty.domain.ProductBean;
import tw.com.ty.service.ProductAjaxService;
import tw.com.ty.util.DatetimeConverter;


@Controller
@ResponseBody
@RequestMapping("/pages/ajax")
@CrossOrigin(origins = {"http://192.168.74.54:8080","http://192.168.74.54:4321", "http://localhost:8080", "http://127.0.0.1:5500","192.168.1.110:8080","http://127.0.0.1:5501","http://127.0.0.1:4321"})
public class ProductAjaxController {

    @Autowired
    private ProductAjaxService productAjaxService;

    @DeleteMapping("/products/{pk}")
    public String remove(@PathVariable(name="pk") Integer id) {
        JSONObject resJson = new JSONObject();

        if(id==null) {
            //notify error message
            resJson.put("message", "id is must");
            resJson.put("success",false);
            //以下是id不存在之處理
        } else if(!productAjaxService.exists(id)) {
            //notify error message
            resJson.put("message", "id does not exist");
            resJson.put("success",false);
        } else {
            boolean rs = productAjaxService.remove(id);
            if (rs == false) {
                //notify error message
                resJson.put("message", "failed to delete");
                resJson.put("success",false);
            } else {
                //success error message
                resJson.put("message", "success");
                resJson.put("success",true);
                System.out.println(rs);
            }
        }
        return resJson.toString();

}
    @PostMapping("/products/find")
    public String findAll(@RequestBody String body) {
        JSONObject resJson = new JSONObject();
        JSONArray array = new JSONArray();
        long count = productAjaxService.count(body);
        resJson.put("count", count);
        List<ProductBean> beans = productAjaxService.find(body);
        if(beans != null && !beans.isEmpty()) {
            for(ProductBean bean : beans){
                String make = DatetimeConverter.toString(bean.getMake(), "yyyy-MM-dd");
                JSONObject product = new JSONObject()
                    .put("id", bean.getId())
                    .put("name",bean.getName())
                    .put("price", bean.getPrice())
                    .put("make",make)
                    .put("expire", bean.getExpire());
                array.put(product);
            }
        }
        resJson.put("list", array);
        return resJson.toString();
    }
    


    @GetMapping("/products/{pk}")
    public String findById(@PathVariable(name="pk") Integer id) {
        System.out.println(111);
        System.out.println(2222);
        JSONObject resJson = new JSONObject();
        JSONArray array = new JSONArray();
        ProductBean bean = productAjaxService.findById(id);
        if(bean != null){
            //data exists
            //把data for db變成json物件
            JSONObject product = new JSONObject()
                .put("id", bean.getId())
                .put("name",bean.getName())
                .put("price", bean.getPrice())
                .put("make",DatetimeConverter.toString(bean.getMake(), "yyyy-MM-dd"))
                .put("expire", bean.getExpire());
            array.put(product);
        }
        resJson.put("list", array);
        return resJson.toString();
    }
    

    @PutMapping("/products/{pk}")
    public String modify(@RequestBody String body,
                        @PathVariable(name="pk") Integer id   
    ) {
        JSONObject resJson = new JSONObject();
        if(id==null) {
            //notify error message
            resJson.put("message", "id is must");
            resJson.put("success",false);
            //以下是id不存在之處理
        } else if(!productAjaxService.exists(id)) {
            //notify error message
            resJson.put("message", "id does not exist");
            resJson.put("success",false);
        } else {
            ProductBean rs = productAjaxService.modify(body);
            //解決rs例外
            if(rs==null) {
                //notify error message
                resJson.put("message", "failed to mod");
                resJson.put("success",false);
            } else {
                //success error message
                resJson.put("message", "success");
                resJson.put("success",true);
            }
        }

        System.out.println(id);
        System.out.println(body);
        return resJson.toString();
    }
    


    @PostMapping("/products")
    public String create(@RequestBody String body) {
        JSONObject resJson = new JSONObject();

        JSONObject obj = new JSONObject(body);
        Integer id = obj.isNull("id") ? null :obj.getInt("id");
        if(id==null) {
            //notify error message
            resJson.put("message", "id is must");
            resJson.put("success",false);

        } else if(productAjaxService.exists(id)) {
            //notify error message
            resJson.put("message", "id is not unique");
            resJson.put("success",false);

        } else {
            ProductBean rs = productAjaxService.create(body);
            //解決rs例外
            if(rs==null) {
                //notify error message
                resJson.put("message", "failed to create");
                resJson.put("success",false);
            } else {
                //success error message
                resJson.put("message", "success");
                resJson.put("success",true);
            }
        }
        return resJson.toString();
    }
}