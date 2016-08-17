package com.adonia.sloth.utils;

/**
 * 全局变量
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public interface IServiceConstant {

    String HTTP_SCHEME = "http://";

    String HTTPS_SCHEME = "https://";

    /**
     * uri分隔符
     */
    String URI_SPLIT_CHAR = "/";

    long SERVICE_NOT_FOUND = 404L;

    long SERVICE_INTERNAL_ERROR = 500L;
}
