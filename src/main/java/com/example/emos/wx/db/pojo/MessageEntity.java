package com.example.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
//发的一方
@Data
@Document(collection = "message")
public class MessageEntity implements Serializable {
    @Id
    private String _id;

    @Indexed(unique = true)
    private String uuid;

    @Indexed
    private Integer senderId;

    private String senderPhoto="https://emos-1311696055.cos.ap-shanghai.myqcloud.com/img/System.jpg";

    private String senderName;

    @Indexed
    private Date sendTime;

    private String msg;
}
