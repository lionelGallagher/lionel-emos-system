package com.example.emos.wx.controller.Form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@ApiModel
@Data
public class DeleteMessageForm {
    @NotBlank
    private String id;
}