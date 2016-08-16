package com.adonia.sloth.service.zk;

import com.adonia.sloth.model.InstanceDetail;
import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.IServiceFinder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/**
 * 从zookeeper注册中心查找服务实例
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public class ZKServiceFinder implements IServiceFinder, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZKServiceFinder.class);

    private final String zkServerUri;

    private final String servicePath;

    private final CuratorFramework client;

    private final ServiceDiscovery<InstanceDetail> discovery;

    public ZKServiceFinder(String zkServerUri, String servicePath) throws ServiceException{
        this.zkServerUri = zkServerUri;
        this.servicePath = servicePath;

        this.client = CuratorFrameworkFactory.newClient(zkServerUri, new ExponentialBackoffRetry(1000, 3));
        client.start();

        this.discovery = ServiceDiscoveryBuilder.builder(InstanceDetail.class)
                .basePath(servicePath)
                .client(client)
                .build();
        try {
            discovery.start();
        } catch (Exception e) {
            LOGGER.error("Failed to start service discovery.", e);
            throw new ServiceException(e);
        }
    }

    /**
     * 根据服务标志名称，查找服务实例
     * <p>
     * N.B. 对于分布式集群场景，同一服务名称，会存在多个实例，这时，会随机从服务列表中选择一个
     *
     * @param serviceName 服务实例名
     * @return
     * @throws ServiceException
     */
    @Override
    public InstanceDetail findService(String serviceName) throws ServiceException {

        try {
            Collection<ServiceInstance<InstanceDetail>> instances = this.discovery.queryForInstances(serviceName);

            if(CollectionUtils.isEmpty(instances)) {
                LOGGER.warn("Could not find any available service within name {}.", serviceName);
                return null;
            }

            int index = new Random().nextInt(instances.size());

            Iterator<ServiceInstance<InstanceDetail>> iter = instances.iterator();
            InstanceDetail instance = IteratorUtils.get(iter, index).getPayload();

            LOGGER.info("Find a service instance {} within service name {}.", instance, serviceName);
            return instance;
        } catch (Exception e) {
            LOGGER.error("Exception happens when finding service with name {}.", serviceName, e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if(null != discovery) {
            discovery.close();
        }

        if(null != client) {
            client.close();
        }
    }
}
