package com.adonia.sloth.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 测试Model
 *
 * @author loulou.liu
 * @create 2016/8/17
 */
public class Person {
    private String id;

    private String name;

    private int age;

    private String location;

    public Person() {
    }

    public Person(int age, String location) {
        this.age = age;
        this.location = location;
    }

    public Person(String name, int age, String location) {
        this(age, location);
        this.name = name;
    }

    public Person(String id, String name, int age, String location) {
        this(name, age, location);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public boolean equals(Object obj) {
        if(null == obj) {
            return false;
        }

        if(obj instanceof Person) {
            Person p = (Person) obj;
            return StringUtils.equals(p.getName(), this.name) && p.getAge() == this.age
                    && StringUtils.equals(p.getLocation(), this.location);
        }

        return false;
    }
}
