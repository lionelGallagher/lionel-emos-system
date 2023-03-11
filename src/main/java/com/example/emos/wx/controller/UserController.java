package com.example.emos.wx.controller;

import cn.hutool.json.JSONUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.config.tencent.TLSSigAPIv2;
import com.example.emos.wx.controller.Form.*;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.MeetingService;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author leach
 * mmbj_18193320486(微信)
 * 具体流程是 拿到前端传过来的邀请码判断
 * 再通过限时的code获取openId
 * 插入 再通过唯一的openId来获取
 * userId生成token进行保存
 */
@Api("测试注册接口")
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserService userService;
    @Resource
    private MeetingService meetingService;
    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;
    @Value("${trtc.appid}")
    private Integer appid;

    @Value("${trtc.key}")
    private String key;

    @Value("${trtc.expire}")
    private Integer expire;

    @ApiOperation("注册用户")
    @PostMapping("/register")
    public R registerController(@Valid @RequestBody RegisterForm form) {
        int userId = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
        Set<String> permissions = userService.searchUserPermissions(userId);
        String token = jwtUtil.creatToken(userId);
        saveCacheToken(token, userId);
        return R.ok("用户保存成功").put("token", token).put("permission", permissions);

    }

    //对redis的插入进行封装
    public void saveCacheToken(String token, int userId) {
        redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);

    }

    @ApiOperation("登录系统")
    @PostMapping("/login")
    public R loginController(@Valid @RequestBody LoginForm form) {
        int userId = userService.isLogin(form.getCode());
        Set<String> permissions = userService.searchUserPermissions(userId);
        String token = jwtUtil.creatToken(userId);
        saveCacheToken(token, userId);
        return R.ok("登录成功").put("token", token).put("permission", permissions);

    }

    @ApiOperation("用户页面信息")
    @GetMapping("/searchUserSummary")
    public R loginController(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap hashMap = userService.searchUserSummary(userId);
        return R.ok().put("result", hashMap);

    }


    @ApiOperation("加载会议人员详情信息")
    @PostMapping("/searchUserGroupByDept")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:SELECT"}, logical = Logical.OR)
    public R SearchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm form) {
        ArrayList<HashMap> list = meetingService.searchUserGroupByDept(form.getKeyword());
        return R.ok().put("result", list);
    }


    @ApiOperation("加载会议人员")
    @PostMapping("/searchMembers")
    @RequiresPermissions(value = {"ROOT", "MEETING:INSERT", "MEETING:UPDATE"}, logical = Logical.OR)
    public R searchMembers(@Valid @RequestBody SearchMembersForm form) {
        if (!JSONUtil.isJsonArray(form.getMembers())) {
            throw new EmosException("不是json数组");
        }
        //转换为数组再转为list
        List<Integer> param = JSONUtil.parseArray(form.getMembers()).toList(Integer.class);
        ArrayList<HashMap> list = userService.searchMembers(param);
        return R.ok().put("result", list);
    }

    @PostMapping("/selectUserPhotoAndName")
    @ApiOperation("查询审批后的人物信息")
    @RequiresPermissions(value = {"WORKFLOW:APPROVAL"})
    public R selectUserPhotoAndName(@Valid @RequestBody SelectUserPhotoAndNameFrom form) {
        if (JSONUtil.isJsonArray(form.getIds())) {
            throw new EmosException("参数不是json数组");
        }
        List<Integer> list = JSONUtil.parseArray(form.getIds()).toList(Integer.class);
        userService.selectUserPhotoAndName(list);
        return R.ok().put("result", list);
    }

    @GetMapping("/genUserSig")
    @ApiOperation("生成用户签名")
    public R genUserSig(@RequestHeader("token") String token) {
        int id = jwtUtil.getUserId(token);
        String email = userService.searchMemberEmail(id);
        TLSSigAPIv2 api = new TLSSigAPIv2(appid, key);
        String userSig = api.genUserSig(email, expire);
        return R.ok().put("userSig", userSig).put("email", email);
    }


}
