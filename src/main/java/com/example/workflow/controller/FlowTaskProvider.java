package com.example.workflow.controller;

import com.example.workflow.api.FlowTaskApi;
import com.example.workflow.service.ActivitiService;
import com.example.workflow.utils.RestResponse;
import com.example.workflow.utils.SecurityUtil;
import com.example.workflow.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Allen
 * @Date: 2018/6/25 13:52
 * @Description: 任务相关
 */
@RestController
@Slf4j
@Api(value = "任务控制服务", tags = "任务控制服务")
public class FlowTaskProvider implements FlowTaskApi {


    @Autowired
    ActivitiService activitiService;

    @Autowired
    SecurityUtil securityUtil;

    /***
     * 查询待办任务
     * @param queryWaitTaskReq
     * @return
     */

    @Override
    @PostMapping(value = "/queryAllWaitTask")
    @ApiOperation(value = "查询所有待办任务", notes = "查询所有待办任务")
    public RestResponse queryAllWaitTask(@RequestBody @ApiParam(value = "查询任务请求对象") @Valid QueryWaitTaskReq queryWaitTaskReq) {
        List list = activitiService.queryWaitTask(queryWaitTaskReq.getFirstResult(), queryWaitTaskReq.getMaxResults());
        return RestResponse.successResult(list);
    }


    /***
     * 查询用户任务列表
     * @return
     */

    @Override
    @PostMapping(value = "/queryTask")
    @ApiOperation(value = "查询任务", notes = "查询任务")
    public RestResponse queryTask(@RequestBody @ApiParam(value = "查询任务请求对象") @Valid QueryTaskReq queryTaskReq) {
        List list = activitiService.queryTask(queryTaskReq.getAssignee(), queryTaskReq.getCandidateUser(), queryTaskReq.getCandidateGroup(), queryTaskReq.getFirstResult(), queryTaskReq.getMaxResults());
        return RestResponse.successResult(list);
    }


    /***
     * 审核任务
     * @param completeTaskReq
     * @return
     */
    @Override
    @PostMapping(value = "/completeTask")
    @ApiOperation(value = "审核任务", notes = "审核任务")
    public RestResponse completeTask(@RequestBody @ApiParam(value = "审核任务请求对象") @Valid CompleteTaskReq completeTaskReq) {
        //审核人不能为空
        if (StringUtils.isBlank(completeTaskReq.getAssignee())) {
            return RestResponse.successResult(-1, "审核人不能为空", null);
        }

        Map<String, Object> param = new HashMap<>();
        //流程是否完成
        param.put("isFinish", false);

        if (completeTaskReq.getIsReviewPass() == 1) {
            activitiService.completeTask(completeTaskReq.getTaskId(), completeTaskReq.getAssignee(), completeTaskReq.getVariables(), param);
        }

        //驳回
        if (completeTaskReq.getIsReviewPass() == 0) {
            activitiService.rejectTask(completeTaskReq.getTaskId(), completeTaskReq.getAssignee(), completeTaskReq.getReturnStart() == 1);
        }
        return RestResponse.successResult(param);
    }

    /****
     * 删除任务
     * @param deleteTaskReq
     * @return
     */

    @Override
    @PostMapping(value = "/deleteTask")
    @ApiOperation(value = "删除任务", notes = "删除任务")
    public RestResponse deleteTask(@RequestBody @ApiParam(value = "删除任务求对象") @Valid DeleteTaskReq deleteTaskReq) {
        activitiService.deleteTask(deleteTaskReq.getTaskId());
        return RestResponse.successResult();
    }

    /***
     * 任务指派
     * @param claimTaskReq
     * @return
     */
    @Override
    @ApiOperation(value = "任务指派", notes = "任务指派")
    @PostMapping(value = "/claimTask")
    public RestResponse claimTask(@RequestBody @ApiParam(value = "指派任务求对象") @Valid ClaimTaskReq claimTaskReq) {
        activitiService.claimTask(claimTaskReq.getTaskId(), claimTaskReq.getAssignee());
        return RestResponse.successResult();
    }


    /****
     * 查询任务历史记录
     * @param queryTaskHistoryReq
     * @return
     */
    @Override
    @ApiOperation(value = "查询任务历史记录", notes = "查询任务历史记录")
    @PostMapping(value = "/queryTaskHistory")
    public RestResponse queryTaskHistory(@RequestBody @ApiParam(value = "查询任务历史记录对象") QueryTaskHistoryReq queryTaskHistoryReq) {
        List list = activitiService.queryTaskHistory(queryTaskHistoryReq.getProcessInstanceId());
        return RestResponse.successResult(list);
    }

}
