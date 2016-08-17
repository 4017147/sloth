package com.adonia.sloth.service;

import com.adonia.sloth.model.InstanceDetail;
import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.zk.ZKServiceFinder;

/**
 * 从注册中心获取服务实例
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public class SimpleServiceFinder {

    public static void main(String[] args) {
        try {
            IServiceFinder finder = new ZKServiceFinder("localhost:2181", "sloth");
            InstanceDetail instance = finder.findService("hello");

            if(null != instance) {
                System.out.println(instance.getRequestUrl());
            }
        } catch (ServiceException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
