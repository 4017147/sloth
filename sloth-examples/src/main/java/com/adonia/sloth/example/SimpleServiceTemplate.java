package com.adonia.sloth.example;

import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.IServiceTemplate;
import com.adonia.sloth.service.rest.RestServiceTemplate;

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
            String response = template.get("hello", null, String.class);
            System.out.println(response);
        } catch (ServiceException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
