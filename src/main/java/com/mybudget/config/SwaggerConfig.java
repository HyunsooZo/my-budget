package com.mybudget.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer {
    /**
     * Swagger Docket을 설정하기 위한 빈 생성
     *
     * @return Swagger Docket 객체
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.my-budget"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    /**
     * API 정보를 설정하기 위한 ApiInfo 객체 생성.
     *
     * @return ApiInfo 객체
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("MY-BUDGET REST API")
                .description("Spring boot를 이용한 MY-BUDGET REST API")
                .version("0.0.1")
                .contact(new Contact("MY-BUDGET", "https://github.com/HyunsooZo", ""))
                .build();
    }

    /**
     * 리소스 핸들러를 추가하기 위해 메서드 재정의
     * <p>
     * 이 메서드는 Swagger UI를 위한 리소스 핸들러 구성
     *
     * @param registry 리소스 핸들러를 등록하는 데 사용되는 ResourceHandlerRegistry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI HTML을 제공하기 위한 리소스 핸들러 추가
        registry
                .addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        // webjar 리소스를 위한 리소스 핸들러 추가 (Swagger UI 종속성)
        registry
                .addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}