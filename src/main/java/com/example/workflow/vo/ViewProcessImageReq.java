package com.example.workflow.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


/**
 * @author Allen
 * @Date: 2018/7/27 10:10
 * @Description: 流程实例处理状态请求对象
 */
@Data
@ApiModel
public class ViewProcessImageReq implements Serializable {


    @ApiModelProperty(value = "实例名称", notes = "实例名称",required = true, dataType = "String")
    @NotBlank(message = "实例名称不能为空")
    private String instanceKey;

    @ApiModelProperty(value = "实例ID", notes = "实例ID", required = true, dataType = "String")
    @NotBlank(message = "实例ID不能为空")
    private String processInstanceId;

}
