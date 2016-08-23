# 概述

## 介绍

<b>[Sloth](https://github.com/leoadonia/sloth)</b>是基于[Spring MVC](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html)框架，通过注解的方式，自动注册和发现服务的`Restful`框架。服务会被注册到[zookeeper](https://zookeeper.apache.org/)或者[etcd](https://github.com/coreos/etcd)中，并提供`Java`和`Javascript`客户端调用服务。

### 依赖

* [apache curator framework](http://curator.apache.org/)
* [spring mvc](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html)

## 开发

### 服务端

服务端须是基于`Spring MVC`框架的。`Sloth`提供两种方式注册服务，一种是作用于`Class`上，即指定类中，除了某些特定的被排除的方法外，其余方法均被注册为服务；一种是作用于`Method`上，也就是只有指定的方法被注册为服务，其余均不会。

* 基于`Class`的服务注册
例如：
```
@RestController
@RequestMapping("/persons")
@SlothService(namespace = "sloth.person", version = "v1", excludes = {"updatePerson"})
public class PersonService2 {

    private final CopyOnWriteArrayList<Person> persons = new CopyOnWriteArrayList<>();

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

    @SlothService
    @RequestMapping
    public List<Person> getPerson() {
        return persons;
    }

    @RequestMapping("/detail")
    @SlothService(serviceName = "getPersonById")
    public Person getPerson(@RequestParam("id") String id) throws ServiceException {

        Person person = getPersonById(id);

        if(null == person) {
            throw new ServiceException("Person Not Found!", 404);
        }

        return person;
    }

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

    public String deletePerson(@PathVariable("id") String id) throws ServiceException {

        Person p = getPersonById(id);

        if(null != p) {
            persons.remove(p);
        }

        return "success";
    }
    ...
}
```

> Note:

> * 首先，后台服务需要使用`Spring MVC`框架实现`Restful`服务，那么，Class必须要使用`@Controller`或者`@RestController`注解。`Sloth`将直接忽略不包含上述注解的Class。

> * 接着，对于方法而言，需要使用`@RequestMapping`注解，如果没有的话，同样会被直接忽略。

看下上述示例代码，`@SlothService`注解可以作用于Class和Method，不过两种作用方式下，使用的属性则不相同。

`@SlothService`的属性介绍如下：


| 名称          | 是否可选      | 默认值  | 作用域| 说明 |
| ------------- |:-------------:| -------:|------:|-----:|
| namespace     | 可选          |  空字符 | Class | 命名空间，类似于模块|
| version       | 可选          |  空字符 | Class | 版本号|
| excludes      | 可选          |  空数组 | Class | 需要排除到注册范围之外的方法名列表|
| excludePattern| 可选          |  空字符 | Class | 需要排除到注册范围之外的方法名匹配|
| serviceName   |  可选         |  方法名 | Method| 服务注册到注册中心的标志|


由上可以看出，只有`serviceName`属性是可以作用于方法自身的，其他的都是Class级别的。另外，`Sloth`是将符合条件的方法作为服务注册的，<b>即一个方法即是一个服务</b>。而服务注册的路径为`{rootPath}/{namespace}/{version}/{serviceName}/{serviceId}`。

> 其中:

> * `rootPath`为服务注册的跟路径，默认是`/sloth/service`，在介绍配置项时再详细介绍。

> * `namespace`，`version`和`serviceName`即对应上表的属性。

> * `serviceId`为服务的Id，是注册时自动生成。

再回到Class上的注解，上述代码中，`@SlothService`作用在Controller上了，那么其下所有标志了`@RequestMapping`的方法都将会被注册为服务，除了`excludes`排除的方法。也就是说，示例代码中，注册的服务为`getPersonById`，`getPerson`，`addPerson`：

zookeeper的数据如下：
```
[zk: localhost:2181(CONNECTED) 3] ls /sloth/service/sloth.person/v1
[getPersonById, getPerson, addPerson]
```

> N.B.

> * `Sloth`在自动发现服务时，如果没有配置`serviceName`的话，会自动取方法名作为`serviceName`。但是，`Sloth`并不关系方法的参数信息和返回类型的，即如果一个类中有多个重载方法的话，请务必使用`serviceName`进行区分，否则，`Sloth`会将其作为同一服务进行注册。

具体示例见 [sloth-examples](https://github.com/leoadonia/sloth/blob/master/sloth-examples/src/main/java/com/adonia/sloth/service/PersonService2.java)。

* 基于`Method`的服务注册

例如：

```
@RestController
@RequestMapping("/persons")
@EnableAutoConfiguration
public class PersonService {

    private final CopyOnWriteArrayList<Person> persons = new CopyOnWriteArrayList<>();

    @SlothService(serviceName = "addPerson")
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

    @SlothService
    @RequestMapping
    public List<Person> getPerson() {
        return persons;
    }

    @RequestMapping("/detail")
    public Person getPerson(@RequestParam("id") String id) throws ServiceException {

        Person person = getPersonById(id);

        if(null == person) {
            throw new ServiceException("Person Not Found!", 404);
        }

        return person;
    }

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

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deletePerson(@PathVariable("id") String id) throws ServiceException {

        Person p = getPersonById(id);

        if(null != p) {
            persons.remove(p);
        }

        return "success";
    }
}
```

具体参考: [sloth-examples](https://github.com/leoadonia/sloth/blob/master/sloth-examples/src/main/java/com/adonia/sloth/service/PersonService.java)。

如下，只有方法上同时有`@SlothService`和`@RequestMapping`注解的，才能被注册为服务。

> N.B. 在方法上，不能使用注解中的`namespace`和`version`属性，此时，会从全局配置中读取，详细信息在配置项中会介绍。

### 客户端

* Java客户端
例如：

```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring/sloth.*.xml"})
public class PersonServiceTest {

    @Resource
    private SlothServiceTemplate slothService;
    
    @Test
    public void testAddPerson2() {
        Person person = new Person("leo", 25, "NJ, JS");

        try {
            final String id = slothService.request(SlothServiceTemplate.SlothRequest.withServiceName("addPerson")
                    .andBody(person))
                    .version("v1")
                    .post(String.class);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetPersons2() {
        try {
            String persons = slothService.request(SlothServiceTemplate.SlothRequest.withServiceName("getPerson"))
                    .version("v1")
                    .get(String.class);
            System.out.println(persons);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
}
```

具体参考: [sloth-examples](https://github.com/leoadonia/sloth/blob/master/sloth-examples/src/test/java/com/adonia/sloth/service/PersonServiceTest.java)。

对于`GET`请求，语法如下:

```
slothService
    .request(
        SlothServieTemplate.SlothRequest
            .withServiceName("serviceName")
            .andParameter("key", "value")
            .andBody(body)
    )
    .namespace("namespace")
    .version("version")
    .get(ResposeType);
```

> Tips:

> * `request`中封装了请求相关属性，包括服务标志---`serviceName`，参数列表，请求体---`body`。

> * 增加参数可以通过级联调用`andParameter(key, value)`的方式，也可直接通过`andParameter(Map(String, Object))`的方式。

> * `namespace`和`version`为可选。

> * `get`指明是`GET`方法，参数为响应类型，如示例代码中的`String.class`。

对于`POST`请求，语法类似：

```
slothService
    .request(
        SlothServieTemplate.SlothRequest
            .withServiceName("serviceName")
            .andParameter("key", "value")
            .andBody(body)
    )
    .namespace("namespace")
    .version("version")
    .post(ResposeType);
```

* Javascript客户端

On the way...

## 配置

配置参数列表:

| 名称        | 是否可选 | 默认值  | 说明|
| ------------- |:-------------:| -----:|------:|-----:|
| sloth.service.namespace      | 可选 | null | 如果不配置，命名空间不起作用|
| sloth.service.version      | 可选      |   null | 如果不配，版本号不起作用|
| sloth.service.path | 可选     |    /sloth/service | 服务注册的跟路径|
| sloth.service.context | 可选     |    / | 访问服务的虚拟路径，如'/platform'|
| sloth.service.port  |  可选  | 8080 |访问服务的端口号 |
| sloth.service.zkServerUri | 可选 | localhost:2181 | 注册中心地址|

> N.B. 此处的`sloth.service.namespace`和`sloth.service.version`为`namespace`和`version`的全局配置；如果在`@SlothService`注解中也指明了`namespace`和`version`，且不为空，则以注解中的为准。

## 下一步...

* 支持 javascript 客户端；
* 注册中心支持 etcd；
* 服务支持SSL，并提供鉴权定制；
* 开放服务发现规则定制；
* 提供监控，统计，服务管理；错误快速发现，定位。



