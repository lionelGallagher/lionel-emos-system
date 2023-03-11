package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.Form.DeleteMessageForm;
import com.example.emos.wx.controller.Form.SearchMessageByIdForm;
import com.example.emos.wx.controller.Form.SearchMessageByPageForm;
import com.example.emos.wx.controller.Form.UpdateUnreadMessageForm;
import com.example.emos.wx.service.MessageService;
import com.example.emos.wx.task.MessageTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

/**
 * @author leach
 * mmbj_18193320486(微信)
 * 1、获取分页消息
 * 2、根据id查询消息
 * 3、把未读更换成已读
 * 4、根据id删除消息
 */
@RestController
@RequestMapping("/message")
@Api("消息模块网络接口")
public class MessageController {
    @Resource
    private MessageTask messageTask;
    @Resource
    private MessageService messageService;
    @Resource
    private JwtUtil jwtUtil;
    @PostMapping("/searchMessageByPage")
    @ApiOperation("获取分页消息")
    public R searchMessageByPage(@Valid @RequestBody SearchMessageByPageForm form, @RequestHeader("token") String token ){
        int  page = form.getPage();
        int length = form.getLength();
        int start =(page-1)*length;
        int userId = jwtUtil.getUserId(token);
        List<HashMap> list = messageService.searchMessageByPage(userId, start, length);
        return  R.ok().put("result",list);
    }
    @PostMapping("/searchMessageById")
    @ApiOperation("根据id查询消息")
    public R searchMessageById(@Valid @RequestBody SearchMessageByIdForm form ){
        String id = form.getId();
        HashMap map = messageService.searchMessageById(id);
        return  R.ok().put("result",map);
    }
    @PostMapping("/updateUnreadMessage")
    @ApiOperation("把未读更换成已读")
    public R updateUnreadMessage(@Valid @RequestBody UpdateUnreadMessageForm form ){
        String id = form.getId();
        long rows = messageService.updateUnreadMessage(id);
        return  R.ok().put("result",rows>=1?true:false);
    }
    @PostMapping("/deleteMessageRefById")
    @ApiOperation("根据id删除消息")
    public R deleteMessageRefById(@Valid @RequestBody DeleteMessageForm form ){
        String id = form.getId();
        long rows = messageService.deleteMessageRefById(id);
        return  R.ok().put("result",rows>=1?true:false);
    }
    @GetMapping("/refreshMessage")
    @ApiOperation("刷新消息")
    public R refreshMessage(@RequestHeader("token") String token  ){
        int userId = jwtUtil.getUserId(token);
        //接受消息
        messageTask.receiveAsync(userId+"");
        //查询消息
        long lastRows = messageService.searchLastCount(userId);
        //查询未读消息
        long unreadRows = messageService.searchUnreadCount(userId);
        return  R.ok().put("lastRows",lastRows).put("unreadRows",unreadRows);
    }
}
