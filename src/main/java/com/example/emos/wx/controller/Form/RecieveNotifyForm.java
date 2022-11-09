package com.example.emos.wx.controller.Form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Data
@ApiModel
public class RecieveNotifyForm {
    @NotBlank
    private String processId;
    @NotBlank
    private String uuid;
    @NotBlank
    private String result;
}

