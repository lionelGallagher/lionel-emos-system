package com.example.emos.wx.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbWorkdayDao {
  Integer  searchTodayIsWorkdays();

  ArrayList<String> searchWorkdayInRange(HashMap hashMap);
}