package com.example.emos.wx.controller.Form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Data
@ApiModel
public class CheckinForm {
    private String address;
    private String country;
//    省
    private String province;
    private String city;

    //区
    private String district;
}

