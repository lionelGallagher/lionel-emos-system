package com.example.emos.wx.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbDeptDao {

    ArrayList<HashMap> searchDeptMembers(String keyword);

}