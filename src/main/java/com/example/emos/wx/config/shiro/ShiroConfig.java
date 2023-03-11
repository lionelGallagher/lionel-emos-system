package com.example.emos.wx.config.shiro;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Configuration
public class ShiroConfig {
    //将Realm封装在securityManager
    @Bean
    public SecurityManager securityManager(OAuth2Realm oAuth2Realm) {
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        defaultWebSecurityManager.setRealm(oAuth2Realm);
        defaultWebSecurityManager.setRememberMeManager(null);
        return defaultWebSecurityManager;
    }

    @Bean
    //将filter封装在ShiroFilterFactoryBean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager, OAuth2Filter oAuth2Filter) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        //过滤
        HashMap<String, Filter> filter = new HashMap<>();
        filter.put("oauth2", oAuth2Filter);
        shiroFilterFactoryBean.setFilters(filter);


        LinkedHashMap<String, String> filterHashMap = new LinkedHashMap<>();
        filterHashMap.put("/webjars/**", "anon");
        filterHashMap.put("/druid/**", "anon");
        filterHashMap.put("/app/**", "anon");
        filterHashMap.put("/sys/login", "anon");
        filterHashMap.put("/swagger/**", "anon");
        filterHashMap.put("/v2/api-docs", "anon");
        filterHashMap.put("/swagger-ui.html", "anon");
        filterHashMap.put("/swagger-resources/**", "anon");
        filterHashMap.put("/captcha.jpg", "anon");
        filterHashMap.put("/user/register", "anon");
        filterHashMap.put("/user/login", "anon");
        filterHashMap.put("/test/**", "anon");
        filterHashMap.put("/**", "oauth2");
        filterHashMap.put("/meeting/recieveNotify", "anon");

        //这里设置的拦截器分为需要不过滤
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterHashMap);
        return shiroFilterFactoryBean;

    }

    //搞一下LifecycleBeanPostProcessor
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    //角色权限封装类
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
}