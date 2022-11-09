package com.example.emos.wx.controller.Form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Data
@ApiModel
public class SearchUserMeetingInMonthForm {
    @Range(min = 2000, max = 9999)
    private Integer year;

    @Range(min = 1, max = 12)
    private Integer month;
}


