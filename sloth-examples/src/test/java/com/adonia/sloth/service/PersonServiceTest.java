package com.adonia.sloth.service;

import com.adonia.sloth.model.Person;
import com.adonia.sloth.model.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * 服务调用测试
 *
 * @author loulou.liu
 * @create 2016/8/17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring/sloth.*.xml"})
public class PersonServiceTest {

    @Resource
    private IServiceTemplate serviceTemplate;

    @Test
    public void testAddPerson() {
        Person person = new Person("leo", 25, "NJ, JS");

        try {
            final String id = serviceTemplate.post("addPerson", person, String.class);
            System.out.println(id);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
}
