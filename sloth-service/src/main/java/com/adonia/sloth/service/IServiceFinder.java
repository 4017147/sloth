package com.adonia.sloth.service;

import com.adonia.sloth.model.InstanceDetail;
import com.adonia.sloth.model.ServiceException;

/**
 * 从服务注册中心查找服务
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public interface IServiceFinder {

    /**
     * 根据服务标志名称，查找服务实例
     *
     * N.B. 对于分布式集群场景，同一服务名称，会存在多个实例，这时，会随机从服务列表中选择一个
     *
     * @param serviceName  服务实例名
     * @return
     * @throws ServiceException
     */
    InstanceDetail findService(final String serviceName) throws ServiceException;
}
