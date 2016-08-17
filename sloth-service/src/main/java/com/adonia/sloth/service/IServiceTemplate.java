package com.adonia.sloth.service;

import com.adonia.sloth.model.ServiceException;

import java.util.Map;

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
     * @param responseType 响应类型
     * @param <T> 返回值
     * @return
     * @throws ServiceException
     */
    <T> T get(final String serviceName, Class<T> responseType) throws ServiceException;

    /**
     *
     * 使用<code>GET</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param params  参数列表
     * @param responseType  响应类型
     * @param <T>
     * @return
     * @throws ServiceException
     */
    <T> T get(final String serviceName, Map<String, ?> params, Class<T> responseType) throws ServiceException;

    /**
     *
     * 使用<code>GET</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param params  参数列表
     * @param pathVariable  路径参数
     * @param responseType  响应类型
     * @param <T>
     * @return
     * @throws ServiceException
     */
    <T> T get(final String serviceName, Map<String, ?> params, String pathVariable, Class<T> responseType) throws ServiceException;

    /**
     *
     * 使用<code>POST</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param responseType  响应类型
     * @param <T>
     * @return
     * @throws ServiceException
     */
    <T> T post(final String serviceName, Class<T> responseType) throws ServiceException;

    /**
     *
     * 使用<code>POST</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param body  请求体
     * @param responseType  响应类型
     * @param <T>
     * @return
     * @throws ServiceException
     */
    <T> T post(final String serviceName, Object body, Class<T> responseType) throws ServiceException;

    /**
     *
     * 使用<code>POST</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param body  请求体
     * @param params  参数列表
     * @param responseType  响应类型
     * @param <T>
     * @return
     * @throws ServiceException
     */
    <T> T post(final String serviceName, Object body, Map<String, ?> params, Class<T> responseType) throws ServiceException;

    /**
     *
     * 使用<code>POST</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param body  请求体
     * @param params  参数列表
     * @param pathVariable  路径参数
     * @param responseType  响应类型
     * @param <T>
     * @return
     * @throws ServiceException
     */
    <T> T post(final String serviceName, Object body, Map<String, ?> params, String pathVariable, Class<T> responseType)
            throws ServiceException;
}
