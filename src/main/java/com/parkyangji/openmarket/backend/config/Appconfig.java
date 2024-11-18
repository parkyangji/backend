package com.parkyangji.openmarket.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.parkyangji.openmarket.backend.interceptor.SessionInterceptor;

@Configuration
public class Appconfig implements WebMvcConfigurer{

        @Autowired
    private SessionInterceptor sessionInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploadFiles/**") // 웹에서 접근 할 url 경로
            .addResourceLocations("file:///Users/parkyangji/uploadFiles/"); // 이쪽에서 갖다써
    }

    // @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
            .addPathPatterns("/mypage/**")
            .addPathPatterns("/admin/**")
            .excludePathPatterns("/admin", "/admin/loginProcess", "/admin/registerProcess", "/admin/register");
        // .addPathPatterns("/board/writeArticlePage")
        // .addPathPatterns("/board/updateArticlePage");
    }
}
