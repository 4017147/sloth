package com.adonia.sloth.model;

import com.adonia.sloth.utils.IServiceConstant;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 服务实例
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public class InstanceDetail {

    /**
     * 将服务注册到服务中心时，用于标识的名称
     */
    private String serviceName;

    /**
     * 服务所在服务端的监听地址，例如: "localhost:8080"
     */
    private String listenAddress;

    /**
     * 服务所在服务端的部署路径，例如: “/platform”
     */
    private String context = IServiceConstant.URI_SPLIT_CHAR;

    /**
     * 实现服务的Controller的映射路径，例如： “/api”
     */
    private String controllerRequestMapping;

    /**
     * 实现服务的方法的映射路径，例如: "/hello"
     */
    private String methodRequestMapping;

    /**
     * 服务的真实访问路径，为"{scheme}://{listenAddress}/{context}/{controllerRequestMapping}/{methodRequestMapping}"；
     * 例如: "http://localhost:8080/platform/api/hello"
     */
    private String requestUrl;

    private InstanceDetail() {
    }

    public static final class InstanceDetailBuilder {

        private String scheme;

        private InstanceDetail instanceDetail;

        public InstanceDetailBuilder() {
            this(IServiceConstant.HTTP_SCHEME);
        }

        public InstanceDetailBuilder(final String scheme) {
            this.scheme = scheme;
            instanceDetail = new InstanceDetail();
        }

        public InstanceDetailBuilder serviceName(final String serviceName) {
            instanceDetail.serviceName = serviceName;
            return this;
        }

        public InstanceDetailBuilder context(final String context) {
            if(StringUtils.isNotEmpty(context)) {
                instanceDetail.context = context;
            }
            return this;
        }

        public InstanceDetailBuilder listenAddress(final String address) {
            instanceDetail.listenAddress = address;
            return this;
        }

        public InstanceDetailBuilder controllerRequestMapping(final String mapping) {
            instanceDetail.controllerRequestMapping = mapping;
            return this;
        }

        public InstanceDetailBuilder methodRequestMapping(final String mapping) {
            instanceDetail.methodRequestMapping = mapping;
            return this;
        }

        public InstanceDetail build() {
            instanceDetail.requestUrl = buildRequestUri();
            return instanceDetail;
        }

        private String buildRequestUri() {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(scheme + instanceDetail.getListenAddress());
            builder.path(instanceDetail.getContext());

            String controllerMapping = instanceDetail.getControllerRequestMapping();
            if(StringUtils.isNotEmpty(controllerMapping)) {
                controllerMapping = StringUtils.startsWith(controllerMapping, IServiceConstant.URI_SPLIT_CHAR) ?
                        controllerMapping : IServiceConstant.URI_SPLIT_CHAR + controllerMapping;
                builder.path(controllerMapping);
            }

            String methodMapping = instanceDetail.getMethodRequestMapping();
            if(StringUtils.isNotEmpty(methodMapping)) {
                methodMapping = StringUtils.startsWith(methodMapping, IServiceConstant.URI_SPLIT_CHAR) ?
                        methodMapping : IServiceConstant.URI_SPLIT_CHAR + methodMapping;
                builder.path(methodMapping);
            }

            return builder.build().toUriString();
        }
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setControllerRequestMapping(String controllerRequestMapping) {
        this.controllerRequestMapping = controllerRequestMapping;
    }

    public void setMethodRequestMapping(String methodRequestMapping) {
        this.methodRequestMapping = methodRequestMapping;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getListenAddress() {
        return listenAddress;
    }

    public String getContext() {
        return context;
    }

    public String getControllerRequestMapping() {
        return controllerRequestMapping;
    }

    public String getMethodRequestMapping() {
        return methodRequestMapping;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
