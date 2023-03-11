package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.Form.CheckinForm;
import com.example.emos.wx.controller.Form.SearchMonthCheckinForm;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@RestController
@RequestMapping("/checkin")
@Api("测试签到web")

public class CheckinController {
    @Value("${emos.image-folder}")
    private String imageFolder;
    @Resource
    private CheckinService checkinService;
    @Resource
    private JwtUtil jwtUtil;
    //入职日期
    @Resource
    private UserService userService;
    //上班时间
    @Resource
    private SystemConstants systemConstants;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("校验是否可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        String checkinMsg = checkinService.validCanCheckin(userId);
        return R.ok(checkinMsg);
    }

    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@RequestHeader("token") String token, @RequestParam("photo") MultipartFile file, @Valid CheckinForm checkinForm) {
        if (file == null) {
            return R.error("没有文件上传");
        }
        String fileName = file.getOriginalFilename().toLowerCase();
        String path = imageFolder + "/" + fileName;
        if (!fileName.endsWith(".jpg")) {
            FileUtil.del(path);
            return R.error("必须提交jpg格式的图片");
        } else {
            try {
                file.transferTo(Paths.get(path));
                int userId = jwtUtil.getUserId(token);
                HashMap param = new HashMap();
                param.put("userId", userId);
                param.put("address", checkinForm.getAddress());
                param.put("country", checkinForm.getCountry());
                param.put("province", checkinForm.getProvince());
                param.put("city", checkinForm.getCity());
                param.put("district", checkinForm.getDistrict());
                param.put("photo", path);
                checkinService.checkin(param);
                return R.ok("签到成功");
            } catch (IOException e) {
                throw new EmosException("图片保存失败");
            } finally {
                FileUtil.del(path);
            }
        }
    }

    @PostMapping("/creatFaceModel")
    @ApiOperation("创建人脸模型")
    public R creatFaceModel(@RequestHeader("token") String token, @RequestParam("photo") MultipartFile file) {
        if (file == null) {
            return R.error("没有文件上传");
        }
        String fileName = file.getOriginalFilename().toLowerCase();
        String path = imageFolder + "/" + fileName;
        if (!fileName.endsWith(".jpg")) {
            FileUtil.del(path);
            return R.error("必须提交jpg格式的图片");
        } else {
            try {
                file.transferTo(Paths.get(path));
                int userId = jwtUtil.getUserId(token);
                checkinService.createFaceModel(userId, path);
                return R.ok("创建人脸模型成功");
            } catch (IOException e) {
                throw new EmosException("图片保存失败");
            } finally {
                FileUtil.del(path);
            }
        }

    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当日签到数据")
    public R searchTodayCheckin(@RequestHeader("token") String token) {
        /**
         * 查询到的本周的开始日期与结束日期做初始化
         * 返回给查每周记录的给返回（大对象）
         * 每天的记录的返回
         */
        int userId = jwtUtil.getUserId(token);
        //获取用户入职日期
        DateTime userHiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        //每天的签到记录
        HashMap map = checkinService.searchTodayCheckin(userId);
        map.put("attendanceTime", systemConstants.getAttendanceTime());
        map.put("closingTime", systemConstants.getAttendanceEndTime());
        //总天数
        map.put("checkinDays", checkinService.searchCheckinDays(userId));
        //判断日期是否在用户入职之前
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        if (userHiredate.isBefore(DateUtil.date())) {
            startDate = userHiredate;
        }
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        HashMap param = new HashMap();
        //往里差三个数就可查到本周date and status
        param.put("userId", userId);

        param.put("endDate", endDate);
        param.put("startDate", startDate);
        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin", list);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询用户当月签到数据")
    public R searchMonthCheckin(@Valid @RequestHeader("token") String token, @RequestBody SearchMonthCheckinForm form) {
        int userId = jwtUtil.getUserId(token);
        //查询入职日期
        DateTime userHiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        //从form中拿取进化
        String month = form.getMonth() < 10 ? "0" + form.getMonth() : form.getMonth().toString();
        DateTime startDate = DateUtil.parse(form.getYear() + "-" + month + "-01");
        //月期早
        if (startDate.isBefore(DateUtil.beginOfMonth(userHiredate))) {
            throw new EmosException("只能查询考勤之后的日期");
        }
        //日期早
        if (startDate.isBefore(userHiredate)) {
            startDate=userHiredate;
        }
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap param = new HashMap();
        param.put("userId",userId);
        param.put("startDate",startDate.toString());
        param.put("endDate",endDate.toString());
        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);
        int sum_1=0, sum_2=0, sum_3=0;

        for (HashMap<String,String> hashMap : list) {
            String type = hashMap.get("type");
            String status = hashMap.get("status");
            if ("工作日".equals(type)){
                if ("正常".equals(status)){
                    sum_1++;
                }else  if ("迟到".equals(status)){
                    sum_2++;
                }else  if ("缺勤".equals(status)){
                    sum_3++;
                }
            }
        }
        return R.ok().put("list",list).put("sum_1",sum_1).put("sum_2",sum_2).put("sum_3",sum_3);
    }

}









































