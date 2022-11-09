package com.example.emos.wx.controller.Form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@ApiModel
@Data
public class SayHelloForm {
@ApiModelProperty("姓名")
//@NotBlank
//@Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$")
    private String name;
}
