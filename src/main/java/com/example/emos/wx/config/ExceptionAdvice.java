package com.example.emos.wx.config;

import com.example.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@RestControllerAdvice
@Slf4j
//校验-》emos异常-》认证异常-》其他异常
public class ExceptionAdvice {
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String exceptionHandler(Exception e) {
        log.info("执行异常", e);
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            return ex.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof EmosException) {
            EmosException ex = (EmosException) e;
            return ex.getMsg();
        } else if (e instanceof UnauthorizedException) {
            return "未授权异常";
        } else {
            return "后端执行异常";
        }
    }
}
