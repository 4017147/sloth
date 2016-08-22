package com.adonia.sloth.service.zk;

import com.adonia.sloth.model.InstanceDetail;
import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.utils.IServiceConstant;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 使用zookeeper提供服务注册
 *
 * @author loulou.liu
 * @create 2016/8/22
 */
@Component
public class ZKServiceRegistry2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZKServiceRegistry2.class);

    @Resource
    private CuratorFramework curatorClient;

    private ServiceDiscovery<InstanceDetail> discovery;

    /**
     * 服务命名空间，可为空
     */
    private String namespace;

    /**
     * 服务版本号，可为空
     */
    private String version;

    /**
     * 服务注册跟路径
     */
    private String servicePath;

    /**
     * 服务运行实例所在的虚拟路径
     */
    private String context;

    public ZKServiceRegistry2 withServicePath(final String servicePath) {
        this.servicePath = servicePath;
        return this;
    }

    public ZKServiceRegistry2 andNamespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public ZKServiceRegistry2 andVersion(final String version) {
        this.version = version;
        return this;
    }

    public ZKServiceRegistry2 andContext(final String context) {
        this.context = context;
        return this;
    }

    public void start() throws ServiceException {
        final String servicePath = buildServicePath();

        discovery = ServiceDiscoveryBuilder.builder(InstanceDetail.class)
                .client(curatorClient)
                .basePath(servicePath)
                .build();

        try {
            LOGGER.info("Start service discovery with base path: {}.", servicePath);
            discovery.start();
        } catch (Exception e) {
            LOGGER.error("Exception happens, failed to start service discovery!", e);
            throw new ServiceException(e);
        }
    }

    /**
     * 注册服务至注册中心
     *
     * @param host                     服务发布时所在服务端IP或者域名
     * @param port                     服务发布时所在服务端的监听地址
     * @param serviceName              服务标识名称
     * @param controllerRequestMapping 服务实现的Controller的对应的映射地址
     * @param methodRequestMapping     服务实现的方法对应的映射地址
     * @throws ServiceException
     */
    public void register(String host, int port, String serviceName, String controllerRequestMapping, String methodRequestMapping) throws ServiceException {
        InstanceDetail instanceDetail = new InstanceDetail.InstanceDetailBuilder()
                .listenAddress(host + ":" + port)
                .context(this.context)
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

        LOGGER.info("Register service instance {} to service path {}.", serviceInstance, buildServicePath());
        try {
            this.discovery.registerService(serviceInstance);
        } catch (Exception e) {
            LOGGER.error("Failed to registry service instance {}.", serviceInstance, e);
            throw new ServiceException(e);
        }
    }

    // 服务注册路径: {servicePath}/{namespace}/{version}/{serviceName}/{serviceId}
    private String buildServicePath() {
        StringBuilder sb = new StringBuilder(servicePath);

        if(StringUtils.isNotEmpty(this.namespace)) {
            sb.append(IServiceConstant.URI_SPLIT_CHAR).append(this.namespace);
        }

        if(StringUtils.isNotEmpty(this.version)) {
            sb.append(IServiceConstant.URI_SPLIT_CHAR).append(this.version);
        }

        return sb.toString();
    }
}
