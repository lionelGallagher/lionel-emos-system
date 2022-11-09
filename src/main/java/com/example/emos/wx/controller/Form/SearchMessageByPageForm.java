package com.example.emos.wx.controller.Form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@ApiModel
@Data
public class SearchMessageByPageForm {
    @NotNull
    @Min(1)
    private int page;

    @NotNull
    @Range(min = 1,max = 40)
    private int length;
}
