package com.example.workflow.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Auther: Allen
 * @Date: 2018/7/27 13:46
 * @Description:
 */
@Data
@ApiModel
public class QueryTaskHistoryReq implements Serializable {

    @ApiModelProperty(value = "实例ID", notes = "实例ID", required = true, dataType = "String")
    @NotBlank(message = "实例ID不能为空")
    private String processInstanceId;

}
