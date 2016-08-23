package com.adonia.sloth.service;

import com.adonia.sloth.annotation.SlothService;
import com.adonia.sloth.model.ServiceException;
import com.adonia.sloth.service.zk.ZKServiceRegistry;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 服务自动发现
 *
 * <code>
 *     @SlothService(namespace = "namespace", version = "version", excludes = {"methodName1"}, excludePattern = "method*")
 *     Public Class ClazzName {
 *         @SlothService(serviceName = "serviceName")
 *         public <T> T methodName() {
 *         }
 *     }
 * </code>
 *
 * @author loulou.liu
 * @create 2016/8/17
 */
@Service
public class SlothServiceDiscovery implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlothServiceDiscovery.class);

    @Value("${sloth.service.port:8080}")
    private int port;

    private final Map<String, Object> beans = new HashMap<>();

    @Autowired
    private ZKServiceRegistry serviceRegistry;

    @Override
    public void afterPropertiesSet() throws Exception {

        for(Object bean: beans.values()) {
            parseService(bean);
        }
    }

    // 只解析标有 @Controller 或者 @RestController注解的Class
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        beans.putAll(applicationContext.getBeansWithAnnotation(Controller.class));
        beans.putAll(applicationContext.getBeansWithAnnotation(RestController.class));
    }

    private String getLocalIp() {
        return "localhost";
    }

    private void parseService(Object bean) throws ServiceException {

        final String controllerMapping = getControllerMapping(bean);

        SlothService controllerSlothService = AnnotationUtils.findAnnotation(bean.getClass(), SlothService.class);
        final String namespace = (null == controllerSlothService) ? StringUtils.EMPTY : controllerSlothService.namespace();
        final String version = (null == controllerSlothService) ? StringUtils.EMPTY : controllerSlothService.version();

        Method[] methods = bean.getClass().getMethods();
        for(Method method: methods) {
            // 方法满足SlothService的条件: 1. 必须是 @RequestMapping 注解的 2. 所在的Class是 @SlothService 注解的，或者方法自身是 @SlothService 注解的
            RequestMapping methodMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
            if(null == methodMapping) {
                continue;
            }

            SlothService methodSlothService = AnnotationUtils.findAnnotation(method, SlothService.class);
            if(null == controllerSlothService && null == methodSlothService) {
                continue;
            }

            if(isMethodExclude(method.getName(), controllerSlothService)) {
                continue;
            }

            // 如果方法上没有 @SlothService 注解，或者注解中的 serviceName 为空，都已方法名称作为服务标志名
            final String serviceName = (null == methodSlothService || StringUtils.isEmpty(methodSlothService.serviceName()))
                    ? method.getName() : methodSlothService.serviceName();

            serviceRegistry.register(getLocalIp(), port, namespace, version, serviceName, controllerMapping,
                    getMethodRequestMapping(methodMapping));
        }
    }

    // 根据Class上的 @SlothService 注解中的 excludes 和 excludePattern 配置，过滤需要注册为服务的方法
    private boolean isMethodExclude(final String methodName, SlothService controllerSlothService) {

        if(null == controllerSlothService) {
            return false;
        }

        final String[] excludes = controllerSlothService.excludes();
        if(ArrayUtils.contains(excludes, methodName)) {
            return true;
        }

        final String excludePattern = controllerSlothService.excludePattern();
        if(StringUtils.isEmpty(excludePattern)) {
            return false;
        }

        Pattern pattern = Pattern.compile(excludePattern);
        Matcher matcher = pattern.matcher(methodName);
        if(matcher.matches()) {
            return true;
        }

        return false;
    }

    // 获取Class上标注的请求路径，@RequestMapping("/xxx/xx")
    private String getControllerMapping(Object bean) {
        RequestMapping mapping = AnnotationUtils.findAnnotation(bean.getClass(), RequestMapping.class);

        if(null == mapping) {
            return StringUtils.EMPTY;
        }

        String[] values = mapping.value();
        if(ArrayUtils.isEmpty(values)) {
            values = mapping.path();
        }

        return ArrayUtils.isEmpty(values) ? StringUtils.EMPTY : values[0];
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
