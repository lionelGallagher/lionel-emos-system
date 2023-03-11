package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbCheckinDao {
    Integer haveCheckin(HashMap param);

    void insert(TbCheckin tbCheckin);






    //每天的签到记录
    HashMap searchTodayCheckin(int userId);

    //一周内的考勤记录
    ArrayList<HashMap>searchWeekCheckin(HashMap hashMap);

    //签到天数
    long searchCheckinDays(int userId);
}