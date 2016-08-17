package com.adonia.sloth.service;

import com.adonia.sloth.model.Person;
import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.rest.RestServiceTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试请求服务
 *
 * @author loulou.liu
 * @create 2016/8/17
 */
public class PersonTemplate {

    public static void main(String[] args) {
        try {
            IServiceTemplate template = new RestServiceTemplate("localhost:2181", "sloth");

//            Person p = new Person("leo", 25, "Nanjing Jiangsu");
//            String id = template.post("addPerson", p, String.class);
//            System.out.println(id);
//
//            Map<String, String> params = new HashMap<>();
//            params.put("id", id);
//            Person person = template.get("getPersonDetail", params, Person.class);
//            System.out.println(p.equals(person));
//
//            Person newPerson = new Person(25, "Nanjing Jiangsu, China");
//
//            String response = template.post("updatePerson", newPerson, null, id, String.class);
//            System.out.println(response);
//
//            Person _p = template.get("getPersonDetail", params, Person.class);
//            System.out.println(_p.getLocation());

            Map<String, String> params = new HashMap<>();
            params.put("id", "1223");
            Person p = template.get("getPersonDetail", params, Person.class);
            System.out.println(null == p);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

}
