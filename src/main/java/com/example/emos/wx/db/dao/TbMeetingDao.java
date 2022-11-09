package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbMeeting;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbMeetingDao {
   List<String> searchUserMeetingInMonth(HashMap hashMap);
    int deleteMeetingById(int id);

    int updateMeetingInfo(HashMap hashMap);
    //    查询参加会议的用户信息
    ArrayList<HashMap> searchMeetingMembers(int id);

    //        查询创建用户 会议（当中）的基本信息
    HashMap searchMeetingById(int id);

    int insertMeeting(TbMeeting tbMeeting);

    //查询会议相关信息
    ArrayList<HashMap> searchMyMeetingListByPage(HashMap hashMap);


    //更新会议条数
    int updateMeetingInstanceId(HashMap hashMap);


    //   参加该会议中属于一个部门的？
    boolean searchMeetingMembersInSameDept(String uuid);
}