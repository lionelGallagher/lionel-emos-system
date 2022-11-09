package com.example.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Component
public class ThreadLocalToken {
    private  ThreadLocal<String> local=new ThreadLocal<>();
    public void setLocal(String token){
        local.set(token);
    }
    public String getLocal(){
        return local.get();
    }
    public void clear(){
       local.remove();
    }
}
