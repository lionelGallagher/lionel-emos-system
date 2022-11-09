package com.example.emos.wx.service.impl;

import com.example.emos.wx.db.dao.MessageDao;
import com.example.emos.wx.db.dao.MessageRefDao;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.example.emos.wx.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private MessageRefDao messageRefDao;
//向Message集合插入数据
    @Override
    public String insertMessage(MessageEntity entity) {
        String id = messageDao.insert(entity);
        return id;
    }
//根据id查询消息
    @Override
    public HashMap searchMessageById(String id) {
        HashMap map = messageDao.searchMessageById(id);
        return map;
    }
    //查询分页数据
    @Override
    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        List<HashMap> list = messageDao.searchMessageByPage(userId, start, length);
        return list;
    }










    //向ref集合插入数据
    @Override
    public String insertRef(MessageRefEntity entity) {
        String id = messageRefDao.insert(entity);
        return id;
    }
//查询未读消息的数量
    @Override
    public long searchUnreadCount(int userId) {
        long count = messageRefDao.searchUnreadCount(userId);
        return count;
    }
//接受到的最新消息的数量
    @Override
    public long searchLastCount(int userId) {
        long count = messageRefDao.searchLastCount(userId);
        return count;
    }


//将未读改为已读
    @Override
    public long updateUnreadMessage(String id) {
        long rows = messageRefDao.updateUnreadMessage(id);
        return rows;
    }
//根据id删除消息
    @Override
    public long deleteMessageRefById(String id) {
        long rows = messageRefDao.deleteMessageRefById(id);
        return rows;
    }
//根据userId查询消息
    @Override
    public long deleteUserMessageRef(int userId) {
        long rows=messageRefDao.deleteUserMessageRef(userId);
        return rows;
    }
}

