package com.example.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String parameter = super.getParameter(name);
        if (!StrUtil.hasEmpty(parameter)) {
            HtmlUtil.filter(parameter);
        }
        return parameter;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] parameterValues = super.getParameterValues(name);
        if (parameterValues != null) {
            for (int i = 0; i < parameterValues.length; i++) {
                if (!StrUtil.hasEmpty(parameterValues[i])) {
                    String filterparameterValue = HtmlUtil.filter(parameterValues[i]);
                    parameterValues[i] = filterparameterValue;
                }
            }
        }
        return parameterValues;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = super.getParameterMap();
        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        if (parameterMap != null) {
            for (String key : parameterMap.keySet()) {
                String[] values = parameterMap.get(key);
                for (int i = 0; i < values.length; i++) {
                    if (!StrUtil.hasEmpty(values[i])) {
                        String value = HtmlUtil.filter(values[i]);
                        values[i] = value;
                    }
                }
                map.put(key, values);
            }
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        String header = super.getHeader(name);
        if (!StrUtil.hasEmpty(header)) {
            HtmlUtil.filter(header);
        }
        return header;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream in = super.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuffer body = new StringBuffer();
        String line = bufferedReader.readLine();
        while (line != null) {
            body.append(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        inputStreamReader.close();
        in.close();
        Map<String, Object> map = JSONUtil.parseObj(body.toString());
        Map<String, Object> res = new LinkedHashMap<>();
        map.forEach((key, value) -> {
            Object val = map.get(key);
            if (val instanceof String) {
                if (!StrUtil.hasEmpty(val.toString())) {
                    res.put(key, HtmlUtil.filter(val.toString()));
                }
            } else {
                res.put(key, val);
            }
        });
        String jsonStr = JSONUtil.toJsonStr(res);
        ByteArrayInputStream bain = new ByteArrayInputStream(jsonStr.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bain.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };

    }
}
