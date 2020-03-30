package com.example.workflow.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;

/**
 * @ClassName: StartProcessReq
 * @description:
 * @author: Allen
 * @create: 2020-03-11 17:52
 **/
@Data
public class StartProcessReq implements Serializable {


    @ApiModelProperty(value = "实例名称", notes = "实例名称", required = true, dataType = "String")
    @NotBlank(message = "实例名称不能为空")
    private String instanceKey;


    @ApiModelProperty(value = "流程发起人", notes = "流程发起人", required = true, dataType = "String")
    @NotBlank(message = "流程发起人不能为空")
    private String assignee;


    @ApiModelProperty(value = "自定义参数", notes = "自定义参数", dataType = "Map")
    private Map<String, Object> variables;

}
