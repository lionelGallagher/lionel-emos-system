package com.example.emos.wx.db.dao;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Repository
public class MessageDao {
    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * db.message.aggregate([
     * {
     * $set: {
     * "id": { $toString: "$_id" }
     * }
     * },
     * {
     * $lookup:{
     * from:"message_ref",
     * localField:"id",
     * foreignField:"messageId",
     * as:"ref"
     * },
     * },
     * { $sort: {sendTime : -1} },
     * { $skip: 0 },
     * { $limit: 50 },
     * {
     * $project:{
     * id:"$id",
     * msg:"$msg",
     * ref:{
     * $filter:{
     * input:"$ref",
     * as:"item",
     * cond:{
     * $eq:["$$item.receiverId",1]
     * }
     * }
     * },
     * sendTime:"$sendTime",
     * senderId:"$senderId",
     * senderName:"$senderName",
     * senderPhoto:"$senderPhoto",
     * uuid:"$uuid"
     * }
     * }
     * ])
     */
    public String insert(MessageEntity entity) {
        Date sendTime = entity.getSendTime();
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, 8);
        entity.setSendTime(sendTime);
        mongoTemplate.save(entity);
        return entity.get_id();
    }

    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        JSONObject json = new JSONObject();
        json.set("$toString", "$_id");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.addFields().addField("id").withValue(json).build(),
                //关联集合
                Aggregation.lookup("message_ref", "id", "messageId", "ref"),
                //where条件
                Aggregation.match(Criteria.where("ref.receiverId").is(userId)),
                //根据时间降序
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "sendTime")),
                //页起始位置
                Aggregation.skip(start),
                //页大小
                Aggregation.limit(length)
        );
        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "message", HashMap.class);
        List<HashMap> list = results.getMappedResults();
        list.forEach(one -> {
            List<MessageRefEntity> refList = (List<MessageRefEntity>) one.get("ref");
            MessageRefEntity entity = refList.get(0);
            Boolean readFlag = entity.getReadFlag();
            String refId = entity.get_id();
            one.remove("ref");
            one.remove("_id");
            one.put("readFlag", readFlag);
            one.put("refId", refId);
            //把格林尼值时间转化为北京时间
            Date sendTime = (Date) one.get("sendTime");
            sendTime = DateUtil.offset(sendTime, DateField.HOUR, -8);
            String today = DateUtil.today();
            //如果是景天的消息只显示发送时间
            if (today.equals(DateUtil.date(sendTime).toDateStr())) {
                one.put("sendTime", DateUtil.format(sendTime, "HH:mm"));
            } else {
                one.put("sendTime", DateUtil.format(sendTime, "yyyy/MM/dd"));
            }
        });
        return list;
    }

    public HashMap searchMessageById(String id) {
        HashMap map = mongoTemplate.findById(id, HashMap.class, "message");
        //把格林尼值时间转化为北京时间
        Date sendTime = (Date) map.get("sendTime");
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, -8);
        map.replace("sendTime", DateUtil.format(sendTime, "yyyy-MM-dd HH:mm"));
        return map;
    }

}
