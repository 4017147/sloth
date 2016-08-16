package com.adonia.sloth.service.rest;

import com.adonia.sloth.model.InstanceDetail;
import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.IServiceFinder;
import com.adonia.sloth.service.IServiceTemplate;
import com.adonia.sloth.service.zk.ZKServiceFinder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 使用RestTemplate实现服务调用
 *
 * @author loulou.liu
 * @create 2016/8/16
 */
public class RestServiceTemplate implements IServiceTemplate {

    private RestTemplate template;

    private IServiceFinder serviceFinder;

    public RestServiceTemplate(String zkServerUri, String servicePath) throws ServiceException {
        this.template = new RestTemplate();
        this.serviceFinder = new ZKServiceFinder(zkServerUri, servicePath);
    }

    /**
     * 使用<code>GET</code>方法请求服务
     *
     * @param serviceName  服务标志名
     * @param body         请求体，可为空
     * @param responseType 响应类型
     * @return
     * @throws ServiceException
     */
    @Override
    public <T> T get(String serviceName, Object body, Class<T> responseType) throws ServiceException {

        InstanceDetail instance = this.serviceFinder.findService(serviceName);
        if(null == instance) {
            throw new ServiceException("Service Not Found!", 404);
        }

        final String requestUri = instance.getRequestUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json");

        HttpEntity<Object> entry = new HttpEntity<>(body, headers);

        ResponseEntity<T> response = this.template.exchange(requestUri, HttpMethod.GET, entry, responseType);

        if(null != response) {
            return response.getBody();
        }

        return null;
    }
}
