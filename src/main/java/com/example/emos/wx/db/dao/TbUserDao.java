package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mapper
public interface TbUserDao {
   String  searchMemberEmail(int id);
    //会议已经审批的人的信息
    List<HashMap> selectUserPhotoAndName(List param);
    //用户页面信息
    HashMap searchUserSummary(int userId);

    String searchUserHiredate(int userId);

    //检查是不是超级管理员
    boolean haveRootUser();

    //插入用户数据
    int insert(HashMap param);

    //通过查询openId查到用户userId
    Integer searchIdByOpenId(String openId);

    //查询用户的权限
    Set<String> searchUserPermissions(int userId);

    TbUser searchById(int userId);

    HashMap searchNameAndDept(int userId);

    ArrayList<HashMap> searchUserGroupByDept(String keyword);


    ArrayList<HashMap> searchMembers(List param);

    //查询用户信息
    public HashMap searchUserInfo(int userId);

    //查询所属该部门经理的id
    public int searchDeptManagerId(int id);

    //    查询总经理的id
    public int searchGmId();


}