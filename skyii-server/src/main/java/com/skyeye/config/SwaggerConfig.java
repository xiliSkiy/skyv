package com.skyeye.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI skyeyeOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("SkyEye智能监控系统API文档")
                        .description("SkyEye智能监控系统后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SkyEye团队")
                                .url("https://www.skyeye.com")
                                .email("contact@skyeye.com"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
} 