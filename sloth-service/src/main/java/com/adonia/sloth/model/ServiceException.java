package com.adonia.sloth.model;

/**
 * 服务发现统一异常
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public class ServiceException extends Exception {

    static final long serialVersionUID = -1L;

    private long errorCode;

    public ServiceException(String msg, long errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public ServiceException(long errorCode) {
        this("Exception happens in sloth.", errorCode);
    }

    public ServiceException() {
        this(500L);
    }

    public ServiceException(Throwable t) {
        super(t);
        this.errorCode = 500L;
    }
}
