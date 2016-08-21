package com.adonia.sloth.service.rest;

import com.adonia.sloth.model.InstanceDetail;
import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.IServiceFinder;
import com.adonia.sloth.utils.IServiceConstant;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务调用端
 *
 * 服务的存储位置为 {servicePath}/{namespace}/{version}/{serviceName}/{serviceId}
 *
 * <pre>
 *     @Autowired
 *     private SlothServiceTemplate template;
 *
 *     template.namespace("namespace")
 *             .version("version")
 *             .request(SlothRequest.withServiceName("serviceName")
 *             .andParameter("key", "value"))
 *             .get(String.class);
 * </pre>
 *
 * @author loulou.liu
 * @create 2016/8/19
 */
@Service
public class SlothServiceTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlothServiceTemplate.class);

    @Autowired
    private IServiceFinder serviceFinder;

    // @Autowired
    private RestTemplate restTemplate = new RestTemplate();

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

    /**
     * 请求参数列表
     */
    private Map<String, Object> params;

    /**
     * 请求体,可为空
     */
    private Object body;

    /**
     * 路径参数,例如: http://127.0.0.1/8080/sloth/:id 其中的<b>id</b>
     */
    private String pathVariable;

    public SlothServiceTemplate namespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public SlothServiceTemplate version(final String version) {
        this.version = version;
        return this;
    }

    public SlothServiceTemplate request(SlothRequest request) {
        this.pathVariable = request.getPathVariable();
        this.body = request.getBody();
        this.params = request.getParams();
        this.serviceName = request.getServiceName();

        return this;
    }

    /**
     *  使用<code>GET</code>方法请求服务
     *
     * @param responseType
     * @param <T>
     * @return
     * @throws ServiceException
     */
    public <T> T get(Class<T> responseType) throws ServiceException {
        InstanceDetail instance = serviceFinder.findService(this.serviceName);
        if(null == instance) {
            throw new ServiceException("Service Not Found!", IServiceConstant.SERVICE_NOT_FOUND);
        }

        String requestUri = instance.getRequestUrl();
        requestUri = fetchRequestUri(requestUri, this.params, this.pathVariable);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity entry = new HttpEntity(headers);

        LOGGER.info("SlothRequest method: GET, service name: {}, request url: {}.", serviceName, requestUri);

        try {
            ResponseEntity<T> response = restTemplate.exchange(requestUri, HttpMethod.GET, entry, responseType);
            LOGGER.info("Complete SlothRequest, method: GET, service name: {}, request uri: {}.",
                    serviceName, requestUri);

            if(null != response) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            LOGGER.error("Exception happens, SlothRequest method: GET, service name: {}, request uri: {}.",
                    serviceName, requestUri);
            throw new ServiceException(e);
        }

        return null;
    }

    /**
     *
     * 使用<code>POST</code>方法请求服务
     *
     * @param responseType
     * @param <T>
     * @return
     * @throws ServiceException
     */
    public <T> T post(Class<T> responseType) throws ServiceException {
        InstanceDetail instance = serviceFinder.findService(serviceName);
        if(null == instance) {
            throw new ServiceException("Service Not Found!", IServiceConstant.SERVICE_NOT_FOUND);
        }

        String requestUri = instance.getRequestUrl();
        requestUri = fetchRequestUri(requestUri, params, pathVariable);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity entry = (null == body) ? new HttpEntity(headers) : new HttpEntity(body, headers);

        LOGGER.info("SlothRequest method: POST, service name: {}, request uri: {}.", serviceName, requestUri);

        try {
            ResponseEntity<T> response = restTemplate.exchange(requestUri, HttpMethod.POST, entry, responseType);
            LOGGER.info("Complete SlothRequest, method: POST, service name: {}, request uri: {}.",
                    serviceName, requestUri);
            if (null != response) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            LOGGER.error("Exception happens, SlothRequest method: POST, service name: {}, request uri: {}.",
                    serviceName, requestUri);
            throw new ServiceException(e);
        }

        return null;
    }

    /**
     * 封装请求uri
     *
     * @param uri  基础uri，例如 http://localhost:8081/platform
     * @param params 参数列表，例如 {"id": "123456", "type": "2"}
     * @param pathVariable  路径参数，例如 shops
     * @return  http://localhost:8081/platform/shops?id=123456&type=2
     *
     */
    private String fetchRequestUri(String uri, Map<String, ?> params, String pathVariable) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);

        if(StringUtils.isNotEmpty(pathVariable)) {
            pathVariable = StringUtils.startsWith(pathVariable, IServiceConstant.URI_SPLIT_CHAR) ?
                    pathVariable : IServiceConstant.URI_SPLIT_CHAR + pathVariable;
            builder.path(pathVariable);
        }

        if (MapUtils.isNotEmpty(params)) {
            for(Map.Entry<String, ?> entry: params.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        return builder.build().encode().toUriString();
    }

    /**
     * 请求相关属性
     *
     * <code>
     *     SlothRequest request = SlothRequest.withServiceName("serviceName")
     *                                        .andParameter("key1", "value1")
     *                                        .andPath("pathVariable");
     * </code>
     */
    public static final class SlothRequest {
        /**
         * 服务标志名
         */
        private String serviceName;

        /**
         * 请求参数列表
         */
        private Map<String, Object> params;

        /**
         * 请求体,可为空
         */
        private Object body;

        /**
         * 路径参数,例如: http://127.0.0.1/8080/sloth/:id 其中的<b>id</b>
         */
        private String pathVariable;

        protected SlothRequest(final String serviceName) {
            this.serviceName = serviceName;
        }

        public static SlothRequest withServiceName(final String serviceName) {
            return new SlothRequest(serviceName);
        }

        public SlothRequest andParameter(String key, Object value) {
            if(null == params) {
                params = new HashMap<>();
            }

            params.put(key, value);
            return this;
        }

        public SlothRequest andParameter(Map<String, Object> parameters) {
            if(null == params) {
                params = new HashMap<>();
            }

            params.putAll(parameters);
            return this;
        }

        public SlothRequest andBody(Object body) {
            this.body = body;
            return this;
        }

        public SlothRequest andPath(String pathVariable) {
            this.pathVariable = pathVariable;
            return this;
        }

        public String getServiceName() {
            return serviceName;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public Object getBody() {
            return body;
        }

        public String getPathVariable() {
            return pathVariable;
        }
    }
}