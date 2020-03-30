package com.example.workflow.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/***
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerBaseConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/swagger/**").addResourceLocations("classpath:/static/swagger/");
    }

    @Bean
    @Order(1)
    @ConditionalOnMissingBean
    public Docket createRestApi() {

		List<Parameter> operationParameters = new ArrayList<Parameter>();
		ParameterBuilder parameterBuilder = new ParameterBuilder();
		parameterBuilder.name("Authorization").description("access_token").defaultValue("Bearer ").modelRef(new ModelRef("string")).parameterType("header") 	;
		operationParameters.add(parameterBuilder.build());

        String basePackage = "com.example";
        return new Docket(DocumentationType.SWAGGER_2)
        		.globalOperationParameters(operationParameters)
                .apiInfo(apiInfo())
                .select()
                //加了ApiOperation注解的方法，生成接口文档
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                //包下的类，生成接口文档
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build();
    }

	private ApiInfo apiInfo() {
        String name = "新智道枢";
        String url = "http://mti-sh.cn/";
        String email = "";
        String title = "新智道枢系统接口";
        String description = "通过RESTful API方式访问新智道枢系统API.";     // 详细描述
        String version = "2.0";     // 版本
        String termsOfServiceUrl = "http://mti-sh.cn/terms";
        Contact contact = new Contact(name, url, email);
        return new ApiInfoBuilder()
                .title(title)
                .description(description)
                .version(version)
                .termsOfServiceUrl(termsOfServiceUrl)
                .contact(contact)
                .build();
	}
}
