package com.example.emos.wx.controller.Form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Data
@ApiModel
public class SearchUserGroupByDeptForm {

    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,15}$")
    private String keyword;
}
