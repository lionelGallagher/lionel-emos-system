package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
public interface MessageService {

    String insertMessage(MessageEntity entity);

    String insertRef(MessageRefEntity entity);

    long searchUnreadCount(int userId);

    long searchLastCount(int userId);

    List<HashMap> searchMessageByPage(int userId, long start, int length) ;

    HashMap searchMessageById(String id);

    long updateUnreadMessage(String id) ;

    long deleteMessageRefById(String id);

    long deleteUserMessageRef(int userId);
}
