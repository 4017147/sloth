package com.adonia.sloth.service;

import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.zk.ZKServiceRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @RequestMapping("/greet")
    public String greet(@RequestParam("name") String name) {
        return "Hello " + name;
    }

    @RequestMapping("/hi/{from}")
    public String hi(@PathVariable("from") String from, @RequestParam("to") String to) {
        return "Hi " + to + ", " + from + " says hi to you!";
    }

    public static void main(String[] args) {
        try {
            IServiceRegistry registry = new ZKServiceRegistry("localhost:2181", "sloth");
            registry.register("localhost", 8081, "hello", "/", "hello");
            registry.register("localhost", 8081, "greet", "/", "greet");
            registry.register("localhost", 8081, "hi", "/", "hi");
        } catch (ServiceException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        SpringApplication.run(SimpleService.class, "--server.port=8081");
    }
}
