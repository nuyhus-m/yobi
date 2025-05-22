package com.S209.yobi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI 관련 리소스 핸들러 설정
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
        
//        registry.addResourceHandler("/api-docs/**")
//               .addResourceLocations("classpath:/META-INF/resources/");
                
        // 정적 리소스 핸들러 제거
        // registry.addResourceHandler("/api/**") 제거
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // 기본 Content-Type은 APPLICATION_JSON으로 유지하면서
        // APPLICATION_OCTET_STREAM도 지원하도록 설정
        configurer
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("octet-stream", MediaType.APPLICATION_OCTET_STREAM);
    }
} 
