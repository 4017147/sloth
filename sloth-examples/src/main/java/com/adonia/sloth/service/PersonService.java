package com.adonia.sloth.service;

import com.adonia.sloth.model.Person;
import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.zk.ZKServiceRegistry;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 测试服务
 *
 * @author loulou.liu
 * @create 2016/8/17
 */
@RestController
@RequestMapping("/persons")
@EnableAutoConfiguration
public class PersonService {

    private final CopyOnWriteArrayList<Person> persons = new CopyOnWriteArrayList<>();

    /**
     * <pre>
     * POST http://localhost:8082/persons/add
     * {
     *     "name": "",
     *     "age": 21,
     *     "location": ""
     * }
     * </pre>
     * @param person
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addPerson(@RequestBody Person person) throws ServiceException {
        if(persons.contains(person)) {
            throw new ServiceException("Person already exists!", 500);
        }

        final String id = UUID.randomUUID().toString();
        person.setId(id);
        persons.add(person);

        return id;
    }

    /**
     * <pre>
     * GET http://localhost:8082/persons
     * </pre>
     * @return
     */
    @RequestMapping
    public List<Person> getPerson() {
        return persons;
    }

    /**
     * <pre>
     * GET http://localhost:8082/persons/detail?id=xxx
     * </pre>
     *
     * @param id
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/detail")
    public Person getPerson(@RequestParam("id") String id) throws ServiceException {

        Person person = getPersonById(id);

        if(null == person) {
            throw new ServiceException("Person Not Found!", 404);
        }

        return person;
    }

    /**
     * <pre>
     * POST http://localhost:8082/persons/{id}
     * {
     *     "age": xx,
     *     "location": "xxx"
     * }
     * </pre>
     * @param id
     * @param person
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String updatePerson(@PathVariable("id") String id, @RequestBody Person person) throws ServiceException{

        Person p = getPersonById(id);
        if(null == p) {
            throw new ServiceException("Person Not Found!", 404);
        }

        p.setLocation(person.getLocation());
        p.setAge(person.getAge());
        return "success";
    }

    /**
     * DELETE http://localhost:8082/{id}
     *
     * @param id
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deletePerson(@PathVariable("id") String id) throws ServiceException {

        Person p = getPersonById(id);

        if(null != p) {
            persons.remove(p);
        }

        return "success";
    }

    private Person getPersonById(final String id) {

        for(Person person : persons) {
            if(StringUtils.equals(person.getId(), id)) {
                return person;
            }
        }

        return null;
    }

    public static void main(String[] args) {
        try {
            IServiceRegistry registry = new ZKServiceRegistry("localhost:2181", "sloth");
            registry.register("localhost", 8082, "addPerson", "/", "persons", "add");
            registry.register("localhost", 8082, "getPerson", "/", "persons", "");
            registry.register("localhost", 8082, "getPersonDetail", "/", "persons", "detail");
            registry.register("localhost", 8082, "updatePerson", "/", "persons", "");
            registry.register("localhost", 8082, "deletePerson", "/", "persons", "");
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        SpringApplication.run(PersonService.class, "--server.port=8082");
    }

}
