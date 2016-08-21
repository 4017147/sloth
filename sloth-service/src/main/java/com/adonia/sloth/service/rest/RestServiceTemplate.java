package com.adonia.sloth.service.rest;

import com.adonia.sloth.model.InstanceDetail;
import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.IServiceFinder;
import com.adonia.sloth.service.IServiceTemplate;
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

import java.util.Map;

/**
 * 使用RestTemplate实现服务调用
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
@Service
public class RestServiceTemplate implements IServiceTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceTemplate.class);

    private RestTemplate template;

    @Autowired
    private IServiceFinder serviceFinder;

    public RestServiceTemplate() throws ServiceException {
        this.template = new RestTemplate();
    }

    /**
     * 使用<code>GET</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param responseType 响应类型
     * @return
     * @throws ServiceException
     */
    @Override
    public <T> T get(String serviceName, Class<T> responseType) throws ServiceException {
        return get(serviceName, null, responseType);
    }

    /**
     * 使用<code>GET</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param params       参数列表
     * @param responseType 响应类型
     * @return
     * @throws ServiceException
     */
    @Override
    public <T> T get(String serviceName, Map<String, ?> params, Class<T> responseType) throws ServiceException {
        return get(serviceName, params, null, responseType);
    }

    /**
     * 使用<code>GET</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param params       参数列表
     * @param pathVariable 路径参数
     * @param responseType 响应类型
     * @return
     * @throws ServiceException
     */
    @Override
    public <T> T get(String serviceName, Map<String, ?> params, String pathVariable, Class<T> responseType) throws ServiceException {
        InstanceDetail instance = this.serviceFinder.findService(serviceName);
        if(null == instance) {
            throw new ServiceException("Service Not Found!", IServiceConstant.SERVICE_NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity entry = new HttpEntity(headers);

        String requestUri = instance.getRequestUrl();
        requestUri = fetchRequestUri(requestUri, params, pathVariable);

        LOGGER.info("SlothRequest method: GET, service name: {}, request uri: {}", serviceName, requestUri);
        ResponseEntity<T> response;
        try {
            response = this.template.exchange(requestUri, HttpMethod.GET, entry, responseType);
        } catch (RestClientException e) {
            LOGGER.error("Exception happens, request method: GET, service name: {}, request uri: {}", serviceName, requestUri, e);
            throw new ServiceException(e.getMessage(), IServiceConstant.SERVICE_INTERNAL_ERROR);
        }

        if(null != response) {
            return response.getBody();
        }

        return null;
    }

    /**
     * 使用<code>POST</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param responseType 响应类型
     * @return
     * @throws ServiceException
     */
    @Override
    public <T> T post(String serviceName, Class<T> responseType) throws ServiceException {
        return post(serviceName, null, responseType);
    }

    /**
     * 使用<code>POST</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param body         请求体
     * @param responseType 响应类型
     * @return
     * @throws ServiceException
     */
    @Override
    public <T> T post(String serviceName, Object body, Class<T> responseType) throws ServiceException {
        return post(serviceName, body, null, responseType);
    }

    /**
     * 使用<code>POST</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param body         请求体
     * @param params       参数列表
     * @param responseType 响应类型
     * @return
     * @throws ServiceException
     */
    @Override
    public <T> T post(String serviceName, Object body, Map<String, ?> params, Class<T> responseType) throws ServiceException {
        return post(serviceName, body, params, null, responseType);
    }

    /**
     * 使用<code>POST</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param body         请求体
     * @param params       参数列表
     * @param pathVariable 路径参数
     * @param responseType 响应类型
     * @return
     * @throws ServiceException
     */
    @Override
    public <T> T post(String serviceName, Object body, Map<String, ?> params, String pathVariable, Class<T> responseType) throws ServiceException {
        InstanceDetail instance = this.serviceFinder.findService(serviceName);
        if(null == instance) {
            throw new ServiceException("Service Not Found!", 404);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity entry = (null == body) ? new HttpEntity(headers) : new HttpEntity(body, headers);

        String requestUri = instance.getRequestUrl();
        requestUri = fetchRequestUri(requestUri, params, pathVariable);

        LOGGER.info("SlothRequest method: POST, service name: {}, request uri: {}", serviceName, requestUri);
        ResponseEntity<T> response;
        try {
            response = this.template.exchange(requestUri, HttpMethod.POST, entry, responseType);
        } catch (RestClientException e) {
            LOGGER.error("Exception happens, request method: POST, service name: {}, request uri: {}", serviceName, requestUri, e);
            throw new ServiceException(e.getMessage(), IServiceConstant.SERVICE_INTERNAL_ERROR);
        }

        if(null != response) {
            return response.getBody();
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
}
