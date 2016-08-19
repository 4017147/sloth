package com.adonia.sloth.service;

/**
 * 服务调用端
 *
 * 服务的存储位置为 {servicePath}/{namespace}/{version}/{serviceName}/{serviceId}
 *
 * @author loulou.liu
 * @create 2016/8/19
 */
public class SlothServiceTemplate {

    /**
     * 服务命名空间，可为空
     */
    private String namespace;

    /**
     * 服务版本，可为空
     */
    private String version;

    /**
     * 服务标志名
     */
    private String serviceName;


}
