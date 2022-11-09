package com.example.emos.wx.config.shiro;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private JwtUtil jwtUtil;
    //需要保存token
    @Resource
    private ThreadLocalToken threadLocalToken;
    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    //从请求中获取token
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        String token = HttpServletRequestToken(servletRequest);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return new OAuth2Token(token);
    }

    //判断是否需要走拦截器
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        if (servletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        return false;
    }

    //需要走拦截器的走下面这个方法
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("Content-Type", "text/html;charset=UTF-8");
        //允许跨域请求
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        //请求完以后需要收回，防止内存泄露
        threadLocalToken.clear();
        String token = HttpServletRequestToken(req);
        if (StringUtils.isBlank(token)) {
            resp.getWriter().print("无效的令牌");
            return false;
        }
        //现在我们去验证一下token的合理性
        try {
            jwtUtil.verifierToken(token);
        } catch (TokenExpiredException e) {//token过期该怎么处理
            //过期先查询一下redis里面是否有token 有获取id来创建token
            if (redisTemplate.hasKey(token)) {
                redisTemplate.delete(token);
                int userId = jwtUtil.getUserId(token);
                token = jwtUtil.creatToken(userId);
                //这里不会有并发修改的问题，不用加锁  把两个都保存一下 一个要在缓存里面用
                redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
                //aop切面的时候要过来拿
                threadLocalToken.setLocal(token);
            } else {   // 没有就需要重新登录
                resp.getWriter().print("令牌过期了");
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                return false;
            }
        } catch (JWTDecodeException e) {//token内容有问题
            resp.getWriter().print("无效的令牌");
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        //如果全部都合理那就开始进行认证与授权,走Realm
        return executeLogin(request, response);
    }

    //认证如果失败的话会走下面的这个方法
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        //认证失败 只能打印失败信息了
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("Content-Type", "text/html;charset=UTF-8");
        //允许跨域请求
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        try {
            resp.getWriter().print("认证失败" + e.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        return false;
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }

    //把ServletRequest封装获取一下token
    public String HttpServletRequestToken(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        return token;
    }
}
