package com.adonia.sloth.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SlothService {

    /**
     * 服务标志名
     * N.B. 可为空，如果为空，则去方法名
     *
     * @return
     */
    String serviceName() default "";

    /**
     * 服务命名空间，作用于Controller上，用于区分服务
     *
     * @return
     */
    String namespace() default "";

    /**
     * 服务版本
     *
     * @return
     */
    String version() default "";
}
