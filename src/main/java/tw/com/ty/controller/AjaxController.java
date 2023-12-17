package tw.com.ty.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class AjaxController {
    @PostMapping(path = {"ajax.controller/{data}/{data2}"})
    public String handleMethod(@PathVariable(name = "data") String data, @RequestBody(required = false) String body,@PathVariable(name = "data2") String data2) {

        System.out.println("haha"+data);
        System.out.println("haha"+data2);

        System.out.println(body);
        JSONArray array = new JSONArray().put(1).put(22222);
        JSONObject result = new JSONObject().put("n1", "1").put("n2", array).put("n3", "3");

        return result.toString();
    }
    
}