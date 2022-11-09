package com.example.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.db.dao.TbDeptDao;
import com.example.emos.wx.db.dao.TbMeetingDao;
import com.example.emos.wx.db.dao.TbUserDao;
import com.example.emos.wx.db.pojo.TbMeeting;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.css.CSSStyleRule;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Slf4j
@Service
public class MeetingServiceImpl implements MeetingService {
    @Resource
    private TbDeptDao tbDeptDao;
    @Resource
    private TbUserDao tbUserDao;
    @Resource
    private TbMeetingDao tbMeetingDao;
    @Value("${emos.code}")
    private String code;
    @Value("${emos.recieveNotify}")
    private String recieveNotify;
    @Value("${workflow.url}")
    private String workflow;
    @Resource
    private RedisTemplate redisTemplate;


    @Override
    public void deleteMeetingById(int id) {
        HashMap meeting = tbMeetingDao.searchMeetingById(id);
        String uuid = meeting.get("uuid").toString();
        String instanceId = meeting.get("instanceId").toString();
        DateTime date = DateUtil.parse(meeting.get("date") + " " + meeting.get("start"));
        DateTime now = DateUtil.date();
        if (now.isAfterOrEquals(date.offset(DateField.MINUTE, -20))) {
            throw new EmosException("距离会议开始不足20分钟");
        }
        int row = tbMeetingDao.deleteMeetingById(id);
        if (row != 1) {
            throw new EmosException("会议删除失败");
        }
        //删除会议工作流
        JSONObject json = new JSONObject();
        json.set("uuid", uuid);
        json.set("instanceId", instanceId);
        json.set("code", code);
        json.set("reason", "会议被修改");
        String url = workflow + "workflow/deleteProcessById";
        HttpRequest request = HttpRequest.post(url).
                header("Content-Type", "application/json").
                body(json.toString());
        HttpResponse response = request.execute();
        if (response.getStatus() != 200) {
            throw new EmosException("删除工作流失败");
        }

    }

    @Override
    public void updateMeetingInfo(HashMap param) {
        int id = (int) param.get("id");
        String start = param.get("start").toString();
        String instanceId = param.get("instanceId").toString();
        String date = param.get("date").toString();
        HashMap oldMeeting = tbMeetingDao.searchMeetingById(id);


        Integer creatorId = (Integer) oldMeeting.get("creatorId");
        String uuid = oldMeeting.get("uuid").toString();
        //上面2个参数是不爆露的 只能去查
        int row = tbMeetingDao.updateMeetingInfo(param);
        if (row != 1) {
            throw new EmosException("会议更新失败");
        }
        //删除会议的参数 (instanceId)  (uuid) reason code
        JSONObject json = new JSONObject();
        json.set("uuid", uuid);
        json.set("instanceId", instanceId);
        json.set("code", code);
        json.set("reason", "会议被修改");
        String url = workflow + "workflow/deleteProcessById";
        HttpRequest request = HttpRequest.post(url).
                header("Content-Type", "application/json").
                body(json.toString());
        HttpResponse response = request.execute();
        if (response.getStatus() != 200) {
            throw new EmosException("删除工作流失败");
        }
        //加入工作流需要的参数 (uuid) creatorId date start
        startMeeting(uuid, creatorId, date, start);

    }


    @Override
    public HashMap searchMeetingById(int id) {
        //把用户信息封装在会议信息里面
        HashMap map = tbMeetingDao.searchMeetingById(id);
        ArrayList<HashMap> list = tbMeetingDao.searchMeetingMembers(id);
        map.put("members", list);
        return map;

    }


    @Override
    public void insertMeeting(TbMeeting tbMeeting) {
        int row = tbMeetingDao.insertMeeting(tbMeeting);
        if (row != 1) {
            throw new EmosException("会议添加失败");
        }
        // 开启审批工作流
        startMeeting(tbMeeting.getUuid(), tbMeeting.getCreatorId().intValue(), tbMeeting.getDate(), tbMeeting.getStart());
    }

    @Override
    public ArrayList searchMyMeetingListByPage(HashMap hashMap) {
        ArrayList<HashMap> list = tbMeetingDao.searchMyMeetingListByPage(hashMap);
        ArrayList<HashMap> resultList = new ArrayList<>();      //装日期与数据
        String date = null;
        JSONArray array = null;
        HashMap resultMap;

        for (HashMap map : list) {
            String temp = map.get("date").toString();
            //如果日期不相等就做分页数据
            if (!temp.equals(date)) {
                date = temp;
                array = new JSONArray();
                resultMap = new HashMap();
                resultMap.put("date", date);
                resultMap.put("list", array);
                resultList.add(resultMap);
            }
            //相等的话追加数据  无论是否同一天都要塞数据
            array.put(map);
        }
        return resultList;
    }



    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        ArrayList<HashMap> list1 = tbDeptDao.searchDeptMembers(keyword);
        ArrayList<HashMap> list2 = tbUserDao.searchUserGroupByDept(keyword);
        for (HashMap map1 : list1) {
            long id = (long) map1.get("id");
            ArrayList members = new ArrayList();
            for (HashMap map2 : list2) {
                long deptId = (long) map2.get("deptId");
                if (id == deptId) {
                    members.add(map2);
                }
            }
            map1.put("members", members);
            //也就是在每一个list集合里面的hashmap 又拽了一个list为员工数据
        }
        return list1;
    }



    private void startMeeting(String uuid, int creatorId, String date, String start) {
//查询创建者用户信息
        HashMap info = tbUserDao.searchUserInfo(creatorId);
        JSONObject json = new JSONObject();
        //这里的的url是 当我们交给审批流 以后审批流给我们的通知信息地址
        json.set("url", recieveNotify);
        json.set("uuid", uuid);
        json.set("openId", info.get("openId"));
        json.set("code", code);
        json.set("date", date);
        json.set("start", start);
        String[] roles = info.get("roles").toString().split(",");
        if (!ArrayUtil.contains(roles, "总经理")) {
//            查询总经理和同部门经理的Id
            Integer managerId = tbUserDao.searchDeptManagerId(creatorId);
            Integer gmId = tbUserDao.searchGmId();
            json.set("managerId", managerId);
            json.set("gmId", gmId);
            //查询是不是同一个部门
            boolean bool = tbMeetingDao.searchMeetingMembersInSameDept(uuid);
            json.set("sameDept", bool);
        }
        String url = workflow + "/workflow/startMeetingProcess";
        //请求工作流 开启工作流
        HttpResponse response = HttpRequest.post(url).
                header("Content-Type", "application/json")
                .body(json.toString()).execute();
        if (response.getStatus() == 200) {
            json = JSONUtil.parseObj(response.body());
            String instanceId = json.getStr("instanceId");
            //响应成功就更新会议状态
            HashMap param = new HashMap();
            param.put("uuid", uuid);
            param.put("instanceId", instanceId);
            int rows = tbMeetingDao.updateMeetingInstanceId(param);
            if (rows != 1) {
                throw new EmosException("保存会议工作流失败");
            }
        }
    }
    @Override
    public Long searchRoomIdByUUID(String uuid) {
        Object temp = redisTemplate.opsForValue().get(uuid);
        long roomId = Long.parseLong(temp.toString());
        return roomId;
    }

    //自己会议的数据
    @Override
    public List<String> searchUserMeetingInMonth(HashMap hashMap) {
        List<String> list = tbMeetingDao.searchUserMeetingInMonth(hashMap);
        return list;
    }
}















