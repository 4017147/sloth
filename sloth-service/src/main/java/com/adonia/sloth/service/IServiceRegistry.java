package com.adonia.sloth.service;

import com.adonia.sloth.model.ServiceException;

/**
 * 服务注册中心
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public interface IServiceRegistry {

    /**
     * 注册服务至注册中心
     *
     * @param host  服务发布时所在服务端IP或者域名
     * @param port  服务发布时所在服务端的监听地址
     * @param serviceName 服务标识名称
     * @param context 服务发布时，访问的上下文
     * @param methodRequestMapping 服务实现的方法对应的映射地址
     * @throws ServiceException
     */
    void register(final String host, int port, final String serviceName, final String context,
                  final String methodRequestMapping) throws ServiceException;

    /**
     * 注册服务至注册中心
     *
     * @param host  服务发布时所在服务端IP或者域名
     * @param port  服务发布时所在服务端的监听地址
     * @param serviceName  服务标识名称
     * @param context  服务发布时，访问的上下文
     * @param controllerRequestMapping  服务实现的Controller的对应的映射地址
     * @param methodRequestMapping  服务实现的方法对应的映射地址
     * @throws ServiceException
     */
    void register(final String host, int port, final String serviceName, final String context,
                  final String controllerRequestMapping,final String methodRequestMapping) throws ServiceException;

}
