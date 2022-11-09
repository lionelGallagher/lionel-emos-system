package com.example.emos.wx.controller.Form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@ApiModel
@Data
public class SearchMonthCheckinForm {
    @NotBlank
    @Range(min = 2000,max = 3000)
    private Integer year;
    @NotBlank
    @Range(min = 1,max = 12)
    private Integer month;
}
