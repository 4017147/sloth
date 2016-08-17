package com.adonia.sloth.service;

import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.rest.RestServiceTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务调用
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public class SimpleServiceTemplate {

    public static void main(String[] args) {
        try {
            IServiceTemplate template = new RestServiceTemplate("localhost:2181", "sloth");
            String response = template.get("hello", String.class);
            System.out.println(response);

            Map<String, String> params = new HashMap<>();
            params.put("name", "leo");
            response = template.get("greet", params, String.class);
            System.out.println(response);

            params.clear();
            params.put("to", "leo");
            response = template.get("hi", params, "adonia", String.class);
            System.out.println(response);
        } catch (ServiceException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
