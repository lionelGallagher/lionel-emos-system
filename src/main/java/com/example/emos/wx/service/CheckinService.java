package com.example.emos.wx.service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
public interface CheckinService {
    String validCanCheckin(int userId);

     void checkin(HashMap param);

    void createFaceModel(int userId,String path);

    //每天的签到记录
    HashMap searchTodayCheckin(int userId);

    //一周内的考勤记录
    ArrayList<HashMap> searchWeekCheckin(HashMap hashMap);
    //一月内的考勤记录
    ArrayList<HashMap> searchMonthCheckin(HashMap hashMap);

    //签到天数
    long searchCheckinDays(int userId);
}
