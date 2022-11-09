package com.example.emos.wx.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.db.dao.TbUserDao;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.UserService;
import com.example.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {
    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao userDao;

    @Resource
    private MessageTask messageTask;

    //通过用户传过来的code编码获取openId
    public String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, Object> param = new HashMap<>();
        param.put("appid", appId);
        param.put("secret", appSecret);
        param.put("js_code", code);
        param.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, param);
        JSONObject jsonObject = JSONUtil.parseObj(response);
        String openid = jsonObject.getStr("openid");
        if (openid == null || openid.length() == 0) {
            throw new RuntimeException("登录时的code编码出错");
        }
        return openid;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        return userDao.searchUserSummary(userId);
    }

    @Override
    public String searchUserHiredate(int userId) {
        return userDao.searchUserHiredate(userId);
    }

    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        if (registerCode.equals("000000")) {
            //判断超级管理员是否存在
            boolean bool = userDao.haveRootUser();
            //不存在就插入
            if (!bool) {
                HashMap<String, Object> param = new HashMap<>();
                String openId = getOpenId(code);
                param.put("openId", openId);
                param.put("nickname", nickname);
                param.put("photo", photo);
                param.put("role", "[0]");
                param.put("status", 1);
                param.put("deptName", "管理部");
                param.put("name", "南阳师院");
                param.put("createTime", new Date());
                param.put("root", true);
                param.put("hiredate", new Date());
                userDao.insert(param);
                Integer id = userDao.searchIdByOpenId(openId);
                MessageEntity messageEntity = new MessageEntity();
                messageEntity.setSendTime(new Date());
                messageEntity.setMsg("欢迎注册超级管理员，请及时更新您的员工信息");
                messageEntity.setSenderId(0);
                messageEntity.setSenderName("系统消息");
                messageEntity.setUuid(IdUtil.simpleUUID());
                messageTask.sendAsync(id + "", messageEntity);
                return id;
            } else {
                throw new EmosException("无法绑定超级管理员账号");
            }
        } else {
            //否则就是普通用户

        }
        return 0;
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        Set<String> permissions = userDao.searchUserPermissions(userId);
        return permissions;
    }

    @Override
    public Integer isLogin(String code) {
        String openId = getOpenId(code);
        Integer userId = userDao.searchIdByOpenId(openId);
        if (userId == null) {
            throw new EmosException("账户不存在");
        }
        //接受到的消息条数
//        int i = messageTask.receiveAsync(userId + "");
        return userId;
    }

    @Override
    public TbUser searchById(int userId) {
        TbUser user = userDao.searchById(userId);
        return user;
    }

    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        ArrayList<HashMap> list = userDao.searchMembers(param);
        return list;
    }


    @Override
    public List<HashMap> selectUserPhotoAndName(List param) {
        List<HashMap> list = userDao.selectUserPhotoAndName(param);
        return list;
    }

    @Override
    public String searchMemberEmail(int id) {
        String email = userDao.searchMemberEmail(id);
        return email;
    }
}































































