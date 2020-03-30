package com.example.workflow.controller;

import com.example.workflow.service.ActivitiService;
import com.example.workflow.utils.RestResponse;
import com.example.workflow.vo.IsFinishProcessReq;
import com.example.workflow.vo.QueryProcessInstanceReq;
import com.example.workflow.vo.StartProcessReq;
import com.example.workflow.vo.ViewProcessImageReq;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: ProcessDefinitionProvider
 * @description: 流程处理
 * @author: Allen
 * @create: 2020-03-11 17:27
 **/
@RestController
@Slf4j
@Api(value = "流程处理服务", tags = "流程处理服务")
public class ProcessHandleController {


    @Autowired
    ActivitiService activitiService;

    @ApiOperation(value = "步骤1: 部署工作流规则文件", notes = "部署工作流规则文件")
    @PostMapping(value = "/deploy")
    public RestResponse deploy(@ApiParam(value = "BPMN 流程文件zip压缩包") @RequestParam(value = "file", required = true) MultipartFile file) throws IOException {

        //使用deploy方法发布流程
        if (file == null || file.isEmpty()) {
            return RestResponse.failResult(-100, "Please select a file to upload BPMN file zip.", null);
        }

        activitiService.deploy(file.getInputStream(), file.getOriginalFilename());
        return RestResponse.successResult();
    }


    /***
     * 开始流程
     */
    @PostMapping(value = "/startProcess")
    @ApiOperation(value = "步骤2: 开始流程", notes = "开始流程")
    public RestResponse startProcess(@RequestBody @Validated @ApiParam(value = "开始流程请求对象") @Valid StartProcessReq startProcessReq) {
        //流程配置参数
        Map<String, Object> variables = startProcessReq.getVariables();
        ProcessInstance processInstance = activitiService.startProcess(startProcessReq.getInstanceKey(), startProcessReq.getAssignee(), variables);
        log.debug("processInstance:" + processInstance.getProcessDefinitionId());
        Map result = new HashMap();
        result.put("processInstanceId", processInstance.getId());
        return RestResponse.successResult(result);
    }


    /****
     * 通过key获取流程定义明细列表
     * @return
     */
    @PostMapping(value = "/queryProcess")
    @ApiOperation(value = "通过key获取流程定义明细列表", notes = "通过key获取流程定义明细列表")
    public RestResponse queryProcess(@RequestBody @ApiParam(value = "获取流程定义明细请求对象") @Valid QueryProcessInstanceReq queryProcessInstanceReq) {
        return RestResponse.successResult(activitiService.queryProcess(queryProcessInstanceReq.getInstanceKey()));
    }


    @PostMapping(value = "/isFinishProcess")
    @ApiOperation(value = "流程是否结束", notes = "流程是否结束")
    public RestResponse isFinishProcess(@RequestBody @ApiParam(value = "流程是否结束请求对象") @Valid IsFinishProcessReq isFinishProcessReq) {
        boolean flag = activitiService.isFinishProcess(isFinishProcessReq.getProcessInstanceId());
        Map result = new HashMap();
        result.put("isFinish", flag);
        return RestResponse.successResult(result);
    }


    /****
     * 显示流程实例处理状态图片
     * @param request
     * @param response
     * @param viewProcessImageReq
     * @throws IOException
     */
    @PostMapping(value = "/viewProcessImage")
    @ApiOperation(value = "显示流程实例处理状态图片", notes = "显示流程实例处理状态图片")
    public void viewProcessImage(HttpServletRequest request, HttpServletResponse response, @RequestBody @ApiParam(value = "流程实例处理状态请求对象") @Valid ViewProcessImageReq viewProcessImageReq) throws Exception {
        InputStream imageStream = activitiService.viewProcessImage(viewProcessImageReq.getProcessInstanceId());
        response.setContentType("image/svg+xml");
        OutputStream os = response.getOutputStream();
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = imageStream.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        imageStream.close();
    }

}

