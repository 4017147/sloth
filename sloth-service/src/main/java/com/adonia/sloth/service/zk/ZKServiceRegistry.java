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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;

/**
 * 使用zookeeper提供服务注册
 *
 * @author loulou.liu
 * @create 2016/8/22
 */
@Service
public class ZKServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZKServiceRegistry.class);

    @Resource
    private CuratorFramework curatorClient;

    private ServiceDiscovery<InstanceDetail> discovery;

    /**
     * 服务命名空间，可为空
     */
    @Value("${sloth.service.namespace:#{null}}")
    private String namespace;

    /**
     * 服务版本号，可为空
     */
    @Value("${sloth.service.version:#{null}}")
    private String version;

    /**
     * 服务注册跟路径,默认为<code>/sloth/service</code>
     */
    @Value("${sloth.service.path:/sloth/service}")
    private String servicePath;

    /**
     * 服务运行实例所在的虚拟路径,默认<code>/</code>
     */
    @Value("${sloth.service.context:/}")
    private String context;

    @PostConstruct
    public void start() throws ServiceException {

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

    @PreDestroy
    public void close() {
        if(null != discovery) {
            try {
                discovery.close();
            } catch (IOException e) {
                LOGGER.error("Failed to stop service discovery.", e);
            }
        }
    }

    /**
     * 注册服务至注册中心
     *
     * @param host                     服务发布时所在服务端IP或者域名
     * @param port                     服务发布时所在服务端的监听地址
     * @param namespace                服务命名空间
     * @param version                  服务版本号
     * @param serviceName              服务标识名称
     * @param controllerRequestMapping 服务实现的Controller的对应的映射地址
     * @param methodRequestMapping     服务实现的方法对应的映射地址
     * @throws ServiceException
     */
    public void register(String host, int port, String namespace, String version, String serviceName, String controllerRequestMapping,
                         String methodRequestMapping) throws ServiceException {
        InstanceDetail instanceDetail = new InstanceDetail.InstanceDetailBuilder()
                .listenAddress(host + ":" + port)
                .context(this.context)
                .controllerRequestMapping(controllerRequestMapping)
                .methodRequestMapping(methodRequestMapping)
                .serviceName(serviceName)
                .build();

        ServiceInstance<InstanceDetail> serviceInstance = null;
        serviceName = buildServiceName(namespace, version, serviceName);
        try {
            // 将 service 注册到 {servicePath}/{namespace}/{version}/{serviceName} 下, 并重新设置 service 的标志名称为
            // {namespace}/{version}/{serviceName}。即在查找服务时,亦需将 namespace/version/serviceName组合起来进行查找
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

        LOGGER.info("Register service instance {} to service path {}.", serviceInstance, serviceName);
        try {
            this.discovery.registerService(serviceInstance);
        } catch (Exception e) {
            LOGGER.error("Failed to registry service instance {}.", serviceInstance, e);
            throw new ServiceException(e);
        }
    }

    // 服务注册名称: {namespace}/{version}/{serviceName}
    private String buildServiceName(String namespace, String version, String serviceName) {

        StringBuilder sb = new StringBuilder();

        namespace = StringUtils.isEmpty(namespace) ? this.namespace : namespace;
        if(StringUtils.isNotEmpty(namespace)) {
            sb.append(namespace).append(IServiceConstant.URI_SPLIT_CHAR);
        }

        version = StringUtils.isEmpty(version) ? this.version : version;
        if(StringUtils.isNotEmpty(version)) {
            sb.append(version).append(IServiceConstant.URI_SPLIT_CHAR);
        }

        sb.append(serviceName);
        return sb.toString();
    }
}
