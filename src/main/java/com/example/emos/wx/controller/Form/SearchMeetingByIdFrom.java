package com.example.emos.wx.controller.Form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@ApiModel
@Data
public class SearchMeetingByIdFrom {
    @NotNull
    @Min(1)
    private Integer id;
}

