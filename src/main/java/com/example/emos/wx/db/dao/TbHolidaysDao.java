package com.example.emos.wx.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbHolidaysDao {
  Integer  searchTodayIsHolidays();
  ArrayList<String> searchHolidayInRange(HashMap hashMap);
}