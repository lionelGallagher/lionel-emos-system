package com.example.emos.wx.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig {
    @Bean
    public Docket docket() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        docket.apiInfo(apiInfo()).
                securitySchemes(ApikeyList()).
                securityContexts(contextList()).
                groupName("超哥").
                select().
                apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)).
                paths(PathSelectors.any()).
                build();
        return docket;
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("吕超",
                "https://blog.csdn.net/m0_55928120?spm=1000.2115.3001.5343",
                "leachr@163.com");
        return new ApiInfo("EMOS在线办公系统",
                "leachr Documentation",
                "1.0",
                "urn:tos",
                contact,
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList()
        );
    }

    //规定用户需要什么参数
    private List<ApiKey> ApikeyList() {
        ApiKey apiKey = new ApiKey("token", "token", "header");
        List<ApiKey> ApikeyList = new ArrayList<>();
        ApikeyList.add(apiKey);
        return ApikeyList;
    }

    //四层封装作用域
    private List<SecurityContext> contextList() {
        //如果用户jwt认证通过，则swagger在全局生效
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] scopes = {authorizationScope};

        //存储令牌 和作用域
        SecurityReference securityReference = new SecurityReference("token", scopes);
        ArrayList<SecurityReference> refList = new ArrayList<>();
        refList.add(securityReference);

        SecurityContext ctxList = SecurityContext.builder().securityReferences(refList).build();
        ArrayList<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(ctxList);
        return securityContexts;
    }
}
