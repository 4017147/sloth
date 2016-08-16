package com.adonia.sloth.service.zk;

import com.adonia.sloth.model.InstanceDetail;
import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.IServiceRegistry;
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

/**
 * 服务注册中心，使用zookeeper做服务发现
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public class ZKServiceRegistry implements IServiceRegistry, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZKServiceRegistry.class);

    private final String zkServerUri;

    private final String servicePath;

    private CuratorFramework client;

    private ServiceDiscovery<InstanceDetail> discovery;

    public ZKServiceRegistry(String zkServerUri, String servicePath) throws ServiceException {
        this.zkServerUri = zkServerUri;
        this.servicePath = servicePath;

        client = CuratorFrameworkFactory.newClient(zkServerUri, new ExponentialBackoffRetry(1000, 3));
        client.start();

        discovery = ServiceDiscoveryBuilder.builder(InstanceDetail.class)
                .client(client)
                .basePath(servicePath)
                .build();
        try {
            discovery.start();
        } catch (Exception e) {
            LOGGER.error("Failed to start service discovery.", e);
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

    /**
     * 注册服务至注册中心
     *
     * @param host                 服务发布时所在服务端IP或者域名
     * @param port                 服务发布时所在服务端的监听地址
     * @param serviceName          服务标识名称
     * @param context              服务发布时，访问的上下文
     * @param methodRequestMapping 服务实现的方法对应的映射地址
     * @throws ServiceException
     */
    @Override
    public void register(String host, int port, String serviceName, String context, String methodRequestMapping) throws ServiceException {
        register(host, port, serviceName, context, null, methodRequestMapping);
    }

    /**
     * 注册服务至注册中心
     *
     * @param host                     服务发布时所在服务端IP或者域名
     * @param port                     服务发布时所在服务端的监听地址
     * @param serviceName              服务标识名称
     * @param context                  服务发布时，访问的上下文
     * @param controllerRequestMapping 服务实现的Controller的对应的映射地址
     * @param methodRequestMapping     服务实现的方法对应的映射地址
     * @throws ServiceException
     */
    @Override
    public void register(String host, int port, String serviceName, String context, String controllerRequestMapping, String methodRequestMapping) throws ServiceException {
        InstanceDetail instanceDetail = new InstanceDetail.InstanceDetailBuilder()
                .listenAddress(host + ":" + port)
                .context(context)
                .controllerRequestMapping(controllerRequestMapping)
                .methodRequestMapping(methodRequestMapping)
                .serviceName(serviceName)
                .build();

        ServiceInstance<InstanceDetail> serviceInstance = null;
        try {
            serviceInstance = ServiceInstance.<InstanceDetail>builder()
                    .address(host)
                    .port(port)
                    .name(serviceName)
                    .payload(instanceDetail)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize service instance.", e);
            throw new ServiceException(e);
        }

        if(null == serviceInstance) {
            return;
        }

        LOGGER.info("Register service instance {} to zookeeper {} under node {}.", serviceInstance, zkServerUri, servicePath);
        try {
            this.discovery.registerService(serviceInstance);
        } catch (Exception e) {
            LOGGER.error("Failed to registry service instance {} to zookeeper {}.", serviceInstance, zkServerUri, e);
            throw new ServiceException(e);
        }
    }
}
