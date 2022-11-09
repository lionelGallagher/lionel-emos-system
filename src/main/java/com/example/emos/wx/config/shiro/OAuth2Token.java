package com.example.emos.wx.config.shiro;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Token implements AuthenticationToken {
    private String token;
    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
