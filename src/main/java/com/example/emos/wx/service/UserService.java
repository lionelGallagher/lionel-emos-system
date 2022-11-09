package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.TbUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
public interface UserService {

    public String searchMemberEmail(int id);


    //会议已经审批的人的信息
    List<HashMap> selectUserPhotoAndName(List param);

    HashMap searchUserSummary(int userId);

    //获取用户入职日期
    String searchUserHiredate(int userId);

    //注册获取userId的接口
    int registerUser(String registerCode, String code, String nickName, String photo);

    //通过查询userId查到相关权限的set值
    Set<String> searchUserPermissions(int userId);

    //通过code生成openId
    Integer isLogin(String code);

    //查询用户信息
    TbUser searchById(int userId);

    //会议页面展示人
    ArrayList<HashMap> searchMembers(List param);
}
