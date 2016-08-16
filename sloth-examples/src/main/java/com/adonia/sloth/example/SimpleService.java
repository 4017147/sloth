package com.adonia.sloth.example;

import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.IServiceRegistry;
import com.adonia.sloth.service.zk.ZKServiceRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟服务，Restful
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
@RestController
@EnableAutoConfiguration
public class SimpleService {

    @RequestMapping("/hello")
    public String hello() {
        return "Hello world!";
    }

    public static void main(String[] args) {
        try {
            IServiceRegistry registry = new ZKServiceRegistry("localhost:2181", "sloth");
            registry.register("localhost", 8081, "hello", "/", "hello");
        } catch (ServiceException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        SpringApplication.run(SimpleService.class, "--server.port=8081");
    }
}
