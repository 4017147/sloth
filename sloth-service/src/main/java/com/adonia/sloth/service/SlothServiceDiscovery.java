package com.adonia.sloth.service;

import com.adonia.sloth.annotation.SlothService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务自动发现
 *
 * @author loulou.liu
 * @create 2016/8/17
 */
public class SlothServiceDiscovery implements ApplicationContextAware, InitializingBean {

    private String context;

    private int port;

    public SlothServiceDiscovery(String context, int port) {
        this.context = context;
        this.port = port;
    }

    private final Map<String, Object> beans = new HashMap<>();

    @Autowired
    private IServiceRegistry serviceRegistry;

    @Override
    public void afterPropertiesSet() throws Exception {

        for(Object bean: beans.values()) {

            // 获取Controller上的请求映射路径
            RequestMapping controllerMapping = AnnotationUtils.findAnnotation(bean.getClass(), RequestMapping.class);
            final String controllerRequestMapping = (null == controllerMapping) ? StringUtils.EMPTY : controllerMapping.value()[0];

            // 获取每个方法上的请求映射路径
            Method[] methods = bean.getClass().getMethods();
            for(Method method: methods) {
                // 服务标志名
                // 首先查询方法中是否有“@SlothService”，如果没有，直接跳过，不注册
                SlothService slothService = AnnotationUtils.findAnnotation(method, SlothService.class);
                if(null == slothService) {
                    continue;
                }

                final String serviceName = StringUtils.isEmpty(slothService.serviceName()) ?
                        method.getName() : slothService.serviceName();


                RequestMapping methodMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
                if(null == methodMapping) {
                    continue;
                }

                final String methodRequestMapping = getMethodRequestMapping(methodMapping);

                serviceRegistry.register(getLocalIp(), port, serviceName, context, controllerRequestMapping, methodRequestMapping);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        beans.putAll(applicationContext.getBeansWithAnnotation(Controller.class));
        beans.putAll(applicationContext.getBeansWithAnnotation(RestController.class));
    }

    private String getLocalIp() {
        return "localhost";
    }

    // 获取方法上的路径映射
    // N.B. 对于"@RequestMapping"的情况(即采用跟路径)，"value"和"path"属性均为空，需要注意 IndexOutOfBoundsException
    private String getMethodRequestMapping(RequestMapping mapping) {
        String[] values = mapping.value();
        if(ArrayUtils.isEmpty(values)) {
            values = mapping.path();
        }

        return ArrayUtils.isEmpty(values) ? StringUtils.EMPTY : values[0];
    }
}
