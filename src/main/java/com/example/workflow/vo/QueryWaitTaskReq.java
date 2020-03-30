package com.example.workflow.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: Allen
 * @Date: 2018/7/27 11:22
 * @Description:
 */
@Data
@ApiModel
public class QueryWaitTaskReq implements Serializable {

    @ApiModelProperty(value = "开始记录数", notes = "开始记录数", dataType = "int", example = "1")
    private int firstResult = 0;

    @ApiModelProperty(value = "每页记录数", notes = "每页记录数", dataType = "int", example = "100")
    private int maxResults = 100;

}
