package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
public interface MeetingService {

    void deleteMeetingById(int id);

    public void updateMeetingInfo(HashMap param);


    HashMap searchMeetingById(int id);


    void insertMeeting(TbMeeting tbMeeting);

    ArrayList searchMyMeetingListByPage(HashMap hashMap);

    Long searchRoomIdByUUID(String uuid);


    ArrayList<HashMap> searchUserGroupByDept(String keyword);

    List<String> searchUserMeetingInMonth(HashMap hashMap);
}
