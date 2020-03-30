package com.example.workflow.api;


import com.example.workflow.utils.RestResponse;
import com.example.workflow.vo.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

/**
 * @Auther: Allen
 * @Date: 2018/8/3 15:13
 * @Description:
 */
@RequestMapping("/flowTask")
public interface FlowTaskApi {

    /***
     * 查询待办任务
     * @param queryWaitTaskReq
     * @return
     */
    @PostMapping(value = "/queryWaitTask")
    RestResponse queryAllWaitTask(@RequestBody @Valid QueryWaitTaskReq queryWaitTaskReq);


    /***
     * 查询用户任务列表
     * @return
     */

    @PostMapping(value = "/queryTask")
    RestResponse queryTask(@RequestBody @Valid QueryTaskReq queryTaskReq);


    /***
     * 审核任务
     * @param completeTaskReq
     * @return
     */
    @PostMapping(value = "/completeTask")
    RestResponse completeTask(@RequestBody @Valid CompleteTaskReq completeTaskReq);

    /****
     * 删除任务
     * @param deleteTaskReq
     * @return
     */

    @PostMapping(value = "/deleteTask")
    RestResponse deleteTask(@RequestBody @Valid DeleteTaskReq deleteTaskReq);

    /***
     * 任务指派
     * @param claimTaskReq
     * @return
     */
    @PostMapping(value = "/claimTask")
    RestResponse claimTask(@RequestBody @Valid ClaimTaskReq claimTaskReq);


    /****
     * 查询任务历史记录
     * @param queryTaskHistoryReq
     * @return
     */
    @PostMapping(value = "/queryTaskHistory")
    RestResponse queryTaskHistory(@RequestBody @Valid QueryTaskHistoryReq queryTaskHistoryReq);

}
