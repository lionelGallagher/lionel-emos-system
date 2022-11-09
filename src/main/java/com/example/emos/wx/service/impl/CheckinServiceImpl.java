package com.example.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.dao.*;
import com.example.emos.wx.db.pojo.TbCheckin;
import com.example.emos.wx.db.pojo.TbFaceModel;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {
    @Resource
    private TbHolidaysDao tbHolidaysDao;
    @Resource
    private TbWorkdayDao tbWorkdayDao;
    @Resource
    private TbCheckinDao tbCheckinDao;
    //考勤时间
    @Resource
    private SystemConstants systemConstants;
    @Resource
    private TbFaceModelDao tbFaceModelDao;
    @Resource
    private TbCityDao tbCityDao;
    //py程序进行人脸识别的url
    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;
    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;
    @Value("${emos.email.hr}")
    private String hrEmail;

    @Resource
    private EmailTask emailTask;
    @Resource
    private TbUserDao tbUserDao;


    @Override
    public String validCanCheckin(int userId) {
        boolean bool_1 = tbHolidaysDao.searchTodayIsHolidays() != null ? true : false;
        boolean bool_2 = tbWorkdayDao.searchTodayIsWorkdays() != null ? true : false;
        String type = "工作日";
        if (DateUtil.date().isWeekend()) {
            type = "节假日";
        }
        //这俩条件都可以做特殊判定（互斥但可同时不满足）
        if (bool_1) {
            type = "节假日";
        } else if (bool_2) {
            type = "工作日";
        }
        if (type.equals("节假日")) {
            return "节假日不需要考勤";
        } else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + systemConstants.getAttendanceStartTime();
            String end = DateUtil.today() + " " + systemConstants.getAttendanceEndTime();
            DateTime attendStart = DateUtil.parseDate(start);
            DateTime attendEnd = DateUtil.parseDate(end);
            if (now.before(attendStart)) {
                return "时间过早，不允许打卡";
            } else if (now.after(attendEnd)) {
                return "你已经错过打卡时间";
            } else {
                HashMap map = new HashMap();
                map.put("userId", userId);
//                map.put("date",date);
                map.put("start", start);
                map.put("end", end);
                boolean bool = tbCheckinDao.haveCheckin(map) != null ? true : false;
                return bool ? "可以考勤" : "已经考勤，不用重复考勤";
            }
        }

    }

    @Override
    public void checkin(HashMap param) {
        //判断一下 签到时间
        Date date = DateUtil.date();
        Date date1 = DateUtil.parseDate(DateUtil.today() + "" + systemConstants.getAttendanceTime());
        Date date2 = DateUtil.parseDate(DateUtil.today() + "" + systemConstants.getAttendanceEndTime());
        int status = 1;
        if (date.compareTo(date1) < 0) {
            status = 1;
        } else if (date.compareTo(date1) > 0 && date.compareTo(date2) < 0) {
            status = 2;
        }
        int userId = (Integer) param.get("userId");
        String address = (String) param.get("address");
        String country = (String) param.get("country");
        String province = (String) param.get("province");
        String faceModel = tbFaceModelDao.searchFaceModel(userId);
        if (faceModel == null) {
            throw new EmosException("不存在人脸模型");
        } else {//执行py查询逻辑
            String path = (String) param.get("path");
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(path), "targetModel", faceModel);
            HttpResponse response = request.execute();
            if (response.getStatus() != 200) {
                throw new EmosException("人脸识别异常");
            }
            String body = response.body();
            if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
                throw new EmosException("人脸识别异常");
            } else if ("False".equals(body)) {
                throw new EmosException("签到无效，非本人签到");
            } else if ("True".equals(body)) {


                //检测当地疫情
                int risk = 1;//低风险区
                String city = (String) param.get("city");
                String district = (String) param.get("district");
                if (!StrUtil.isBlank(city) && !StrUtil.isBlank(district)) {
                    String code = tbCityDao.searchCode(city);
                    //查询风险地区
                    try {
                        String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                        Document document = Jsoup.connect(url).get();
                        Element div = document.getElementById("10617");
                        if (div.hasText()) {
                            Elements allElements = div.getAllElements();
                            Element element = allElements.get(1);
                            String result = element.select("p:").text();
                            if ("高风险".equals(result)) {
                                risk = 3;
//                                发送告警邮件

                                HashMap<String, String> map = tbUserDao.searchNameAndDept(userId);
                                String name = map.get("name");
                                String deptName = map.get("dept_name");
                                deptName = deptName != null ? deptName : "";
                                SimpleMailMessage message = new SimpleMailMessage();
                                message.setTo(hrEmail);
                                message.setSubject("员工" + name + "身处高风险疫情地区警告");
                                message.setText(deptName + "员工" + name + "，" +
                                        DateUtil.format(new Date(), "yyyy年MM月dd日") +
                                        "处于" + address +
                                        "，属于新冠疫情高风险地区，请及时与该员工联系，核实情况！");
                                emailTask.sendAsync(message);


                            }
                            if ("中风险".equals(result)) {
                                risk = 2;
                            }
                        }
                    } catch (Exception e) {
                        log.error("检测风险区失败");
                        throw new EmosException("检测风险区失败");
                    }
                }
                //插入数据
                TbCheckin tbCheckin = new TbCheckin();
                tbCheckin.setUserId(userId);
                tbCheckin.setCity(city);
                tbCheckin.setAddress(address);
                tbCheckin.setCountry(country);
                tbCheckin.setDistrict(district);
                tbCheckin.setProvince(province);
                tbCheckin.setRisk(risk);
                tbCheckin.setStatus((byte) status);
                tbCheckin.setCreateTime(date);
                tbCheckinDao.insert(tbCheckin);

            }
        }
    }

    @Override
    public void createFaceModel(int userId, String path) {
        //py创建人脸数据 封装给数据库

        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo", FileUtil.file(path));
        HttpResponse response = request.execute();
        if (response.getStatus() != 200) {
            throw new EmosException("人脸创建异常");
        }
        String body = response.body();
        if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
            throw new EmosException("人脸创建异常");
        } else {
            TbFaceModel tbFaceModel = new TbFaceModel();
            tbFaceModel.setUserId(userId);
            tbFaceModel.setFaceModel(body);
            tbFaceModelDao.insertFaceModel(tbFaceModel);
        }
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        return tbCheckinDao.searchTodayCheckin(userId);

    }

    @Override
    public long searchCheckinDays(int userId) {
        return tbCheckinDao.searchCheckinDays(userId);
    }

    //查询一周当中的签到记录
    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> hashMaps = new ArrayList<>();
        ArrayList<HashMap> DayStatus = tbCheckinDao.searchWeekCheckin(param);
        /**
         *
         *    理一段逻辑:
         *    查到的日期与状态先不急
         *    循环从一周里面拿出一天（判断工作日与节假日）
         *    工作日的情况下-》小于等于当前日期时间，先status为缺勤，其他均为查到的改变数据
         *    存在当天打卡的漏洞（时间未到不能判定status）小于结束之前都为空字符串
         *    bug:如果已经改变打卡状态，但是又修改了别人的状态为空字符串-》已经走了等于可不再走当天打卡判定
         */
        ArrayList<String> holidays = tbHolidaysDao.searchHolidayInRange(param);
        ArrayList<String> workdays = tbWorkdayDao.searchWorkdayInRange(param);
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());
        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        range.forEach(one->{
            String date = one.toDateStr();
            String type="工作日";
            if (one.isWeekend()){
                type="节假日";
           }
            if (holidays!=null&&holidays.contains(date)){
                type="节假日";
            }else  if (workdays!=null&&workdays.contains(date)){
                type="工作日";
            }
            String status="";
            if ("工作日".equals(type)&&DateUtil.compare(DateUtil.date(),one)>=0){//说明查到的已经是过去式
                status="缺勤";
                boolean flag=true;
                for (HashMap<String,String> dayStatus : DayStatus) {
                    if (dayStatus.containsValue(date)){
                       status= dayStatus.get("status");
                        flag=false;
                       break;
                    }
                }
                //对当天时间判断的一个合理性
                DateTime endTime = DateUtil.parse(DateUtil.today() + " " + systemConstants.getAttendanceEndTime());
                String today = DateUtil.today();
                if (date.equals(today) && DateUtil.date().isBefore(endTime)&&flag){
                    status=" ";
                }
            }
            HashMap hashMap = new HashMap();
            hashMap.put("date",date);
            hashMap.put("status",status);
            hashMap.put("type",type);
            hashMap.put("day",one.dayOfWeekEnum().toChinese("周"));
           hashMaps.add(hashMap);

        });

        return hashMaps;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap hashMap) {
        return this.searchWeekCheckin(hashMap);
    }


}



























