package com.ewallet.backend.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("E-Wallet System API Documentation")
                        .version("1.0.0")
                        .description("Tài liệu hướng dẫn và thử nghiệm toàn bộ hệ thống API Ví điện tử E-Wallet")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                // Cấu hình yêu cầu Bảo mật toàn cục cho Swagger UI
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        // Định nghĩa cơ chế xác thực JWT Bearer Token
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập chuỗi JWT Access Token của bạn vào đây (Không cần gõ chữ 'Bearer ')")));
    }
}