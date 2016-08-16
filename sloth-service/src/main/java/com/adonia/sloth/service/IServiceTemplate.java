package com.adonia.sloth.service;

import com.adonia.sloth.model.ServiceException;

/**
 * 服务调用
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public interface IServiceTemplate {

    /**
     *
     * 使用<code>GET</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param body 请求体，可为空
     * @param responseType 响应类型
     * @param <T> 返回值
     * @return
     * @throws ServiceException
     */
    <T> T get(final String serviceName, Object body, Class<T> responseType) throws ServiceException;

}
