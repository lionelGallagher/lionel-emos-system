package com.example.emos.wx.common.util;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
public class R extends HashMap<String, Object> {
    //创造默认对象

    public R() {
        put("msg", "success");
        put("code", HttpStatus.SC_OK);
    }

    //创建 ok对象
    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> msg) {
        R r = new R();
        r.putAll( msg);
        return r;
    }

    public static R ok() {
        return new R();
    }

    //创建 Error对象
    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R error(String msg) {
        return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error() {
        return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "异常错误，请联系管理员");
    }

    //创建 链式put
    public R put(String key,Object value) {
        super.put(key,value);
        return this;
    }
}