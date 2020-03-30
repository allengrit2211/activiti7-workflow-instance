package com.example.workflow.service;


import org.activiti.engine.runtime.ProcessInstance;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/****
 * 工作流服务接口
 */
public interface ActivitiService {

    /***
     * 发布规则
     * @param bpmnName
     */
    void deploy(String bpmnName);


    /***
     * 发布规则
     * @param fileInputStream 文件流
     * @param fileName 文件名称
     */
    void deploy(InputStream fileInputStream, String fileName);

    /***
     *  开始流程
     * @param instanceKey 流程实例key
     * @param assignee 流程发起人
     * @param variables 参数
     */
    ProcessInstance startProcess(String instanceKey, String assignee, Map variables);

    /***
     * 通过key获取流程定义明细列表
     * @param instanceKey
     * @return
     */
    List queryProcess(String instanceKey);

    /***
     * 查询用户任务列表
     * @param assignee 任务执行人
     * @param candidateUser 候选用户
     * @param candidateGroup 候选用户组
     * @return
     */
    List queryTask(String assignee, String candidateUser, String candidateGroup, int firstResult, int maxResults);


    /***
     * 查询任务参数
     * @param taskId
     * @return
     */
    Map<String, Object> queryVariables(String taskId);


    /***
     * 根据实例ID查询任务审核历史
     * @param processInstanceId 实例ID
     * @return
     */
    List queryTaskHistory(String processInstanceId);

    /***
     * 完成任务
     * @param taskId 任务id
     * @param assignee 分配到任务的人
     * @param variables 表单参数信息
     * @param param 返回值
     */
    void completeTask(String taskId, String assignee, Map<String, Object> variables, Map<String, Object> param);

    /****
     * 任务指派
     * @param taskId
     * @param assignee
     */
    void claimTask(String taskId, String assignee);

    void deleteTask(String taskId);


    /****
     * 流程是否完成
     * @param processInstanceId
     * @return
     */
    boolean isFinishProcess(String processInstanceId);


    /***
     * 查询待办任务
     * @param firstResult 开始位置
     * @param maxResults 最大记录数
     * @return
     */
    List queryWaitTask(int firstResult, int maxResults);

    /***
     * 驳回任务
     * @param taskId 当前节点
     * @param returnStart 是否返回到起点
     */
    void rejectTask(String taskId, String assignee, boolean returnStart);

    /****
     * 展示流程处理图
     * @return
     */
    InputStream viewProcessImage(String processInstanceId);
}
