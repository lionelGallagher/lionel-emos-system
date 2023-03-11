package com.example.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.dao.SysConfigDao;
import com.example.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.io.File;

@Slf4j
@ServletComponentScan
@SpringBootApplication
@EnableAsync
public class EmosWxApiApplication {
    @Resource
    public SysConfigDao sysConfigDao;

    @Resource
    public SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;
    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }


    //做一个缓存预热把签到时间的记录保存起来
    @PostConstruct
    public void init() {
        List<SysConfig> sysConfigs = sysConfigDao.selectAllParam();
        sysConfigs.forEach(one -> {
            String paramKey = one.getParamKey();
            paramKey = StrUtil.toCamelCase(paramKey);
            String paramValue = one.getParamValue();
            try {
                Field declaredField = constants.getClass().getDeclaredField(paramKey);
                declaredField.setAccessible(true);
                declaredField.set(constants, paramValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("执行异常", e);
            }
        });
        new File(imageFolder).mkdirs();
    }
}
