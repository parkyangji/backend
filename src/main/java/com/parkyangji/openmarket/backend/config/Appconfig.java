package com.parkyangji.openmarket.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Appconfig implements WebMvcConfigurer{

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploadFiles/**") // 웹에서 접근 할 url 경로
            .addResourceLocations("file:///Users/parkyangji/uploadFiles/"); // 이쪽에서 갖다써
    }
}
