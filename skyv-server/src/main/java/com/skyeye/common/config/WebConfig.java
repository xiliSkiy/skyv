package com.skyeye.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Web配置类
 * 
 * @author SkyEye Team
 */
@Configuration
public class WebConfig {

    /**
     * 配置RestTemplate
     *
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 连接超时5秒
        factory.setReadTimeout(30000);   // 读取超时30秒
        
        return new RestTemplate(factory);
    }
}

