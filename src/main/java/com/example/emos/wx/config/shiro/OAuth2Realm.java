package com.example.emos.wx.config.shiro;

import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {
    //要进行解密所以要用到
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        //判断传进来的token是不是我们的token
        return token instanceof OAuth2Token;
    }

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //登陆时调用
        String token = (String) authenticationToken.getPrincipal();
        int userId = jwtUtil.getUserId(token);
        TbUser user = userService.searchById(userId);
        // 通过token拿到用户的userId,判断是否被冻结
        if (user==null){
            new LockedAccountException("该账户已经被冻结，请联系管理员");
        }
            //往对象里面添加用户信息，token字符串
            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user,token , getName());
        return info;
    }

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        //验证权限时时调用
        TbUser user = (TbUser)principalCollection.getPrimaryPrincipal();
        Integer userId = user.getId();
        //查询权限列表
        Set<String> permissions = userService.searchUserPermissions(userId);
        //把查询到的权限放在info里面
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permissions);
        return info;
    }
}
