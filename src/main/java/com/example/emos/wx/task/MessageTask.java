package com.example.emos.wx.task;

import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.MessageService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Slf4j
@Component
public class MessageTask {
    @Resource
    private ConnectionFactory connectionFactory;
    @Resource
    private MessageService messageService;

    /**
     * 同步发送消息
     *
     * @Param topic 主题
     * @Param entity 消息对象
     */
    public void send(String topic, MessageEntity messageEntity) {
        String id = messageService.insertMessage(messageEntity);
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel();
        ) {
            //连接到某个topic      持久化，锁，自动删除
            //配置信息
            channel.queueDeclare(topic, true, false, false, null);
            HashMap header = new HashMap();
            header.put("messageId", id);
            //属性文件
            AMQP.BasicProperties basicProperties = new AMQP.BasicProperties().builder().headers(header).build();
            //发送消息
            channel.basicPublish("", topic,
                    basicProperties,
                    messageEntity.getMsg().getBytes(StandardCharsets.UTF_8));
            log.debug("消息发送成功");
        } catch (Exception e) {
            log.error("执行异常", e);
            throw new EmosException("消息发送失败");
        }
    }

    /**
     * 异步发送消息
     *
     * @Param topic 主题
     * @Param entity 消息对象
     */
    @Async
    public void sendAsync(String topic, MessageEntity messageEntity) {
        send(topic, messageEntity);
    }

    /**
     * 同步接受消息
     *
     * @Param topic 主题
     */
    public int receive(String topic) {
        int i = 0;
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel();
        ) {
            //连接到某个topic     接受话题 持久化，锁，自动删除
            channel.queueDeclare(topic, true, false, false, null);
            while (true) {
                GetResponse response = channel.basicGet(topic, false);
                if (response != null) {
                    AMQP.BasicProperties props = response.getProps();
                    Map<String, Object> headers = props.getHeaders();
                    String messageId = headers.get("messageId").toString();

                    byte[] body = response.getBody();
                    String message = new String(body);
                    log.debug("从mq接受到的消息:" + message);

                    MessageRefEntity entity = new MessageRefEntity();
                    entity.setMessageId(messageId);
                    entity.setReceiverId(Integer.parseInt(topic));
                    entity.setReadFlag(false);
                    entity.setLastFlag(true);
                    messageService.insertRef(entity);
                    //手动ack
                    long deliveryTag = response.getEnvelope().getDeliveryTag();
                    channel.basicAck(deliveryTag, false);
                    i++;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("执行异常", e);
            throw new EmosException("消息接受失败");
        }
        return i;
    }

    /**
     * 异步接受消息
     *
     * @Param topic 主题
     */
    @Async
    public int receiveAsync(String topic) {
        return receive(topic);
    }

    /**
     * 同步删除消息
     *
     * @Param topic 主题
     */
    public void delete(String topic) {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel();
        ) {
            channel.queueDelete(topic);
            log.debug("消息删除成功");
        } catch (Exception e) {
            log.error("执行异常", e);
            throw new EmosException("消息删除失败");
        }
    }

    /**
     * 异步删除消息
     *
     * @Param topic 主题
     */
    @Async
    public void deleteAsync(String topic) {
        delete(topic);
    }
}
