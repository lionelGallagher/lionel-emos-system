package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.controller.Form.SayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@ServletComponentScan
@RestController
@Api("测试Api接口")
@RequestMapping("test")
public class TestController {
    @PostMapping("sayHello")
    @ApiOperation("最简单的测试方法")
    public R sayHello(@Valid @RequestBody SayHelloForm form) {
        return R.ok().put("message", "Hello," + form.getName());
    }

    @PostMapping("addUser")
    @ApiOperation("添加用户")
    @RequiresPermissions(value = {"ROOT", "USER:ADD"}, logical = Logical.OR)
    public R addUser() {
        return R.ok("用户添加成功");

    }


}
