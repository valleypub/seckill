package com.xxxx.seckill.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * MVC配置类
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserArgumentResolver userArgumentResolver;//方法形参User类型的解析器
    @Autowired
    private AccessLimitInterceptor accessLimitInterceptor;//用于限流的拦截器

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        //这个拦截器可以指定拦截多种类型的参数，即是一个List
        //我们只需要将我们想要进行拦截、验证的参数类型，只需要做下面3步：
        // 1.写一个类implements HandlerMethodArgumentResolver接口，实现它的两个方法
        // 2.将写好的类如：UserArgumentResolver -- 处理User类型的参数，注入到WebConfig类，然后resolvers.add(userArgumentResolver);加入到拦截组中即可
        resolvers.add(userArgumentResolver);
    }
    //这个是防止拦截静态图片的，不加这个图片显示不出来
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLimitInterceptor);
    }
}
