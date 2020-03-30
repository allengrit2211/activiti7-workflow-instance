package com.example.workflow.service.impl;

import com.example.workflow.service.ActivitiService;
import com.example.workflow.vo.HistoricTaskInstanceVo;
import com.example.workflow.vo.ProcessDefinitionVo;
import com.example.workflow.vo.TaskVo;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManagerImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * @author Allen
 * @Date: 2018/6/27 9:47
 * @Description: 工作流服务接口实现
 */
@Service
@Slf4j
public class ActivitiServiceImpl implements ActivitiService {

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    RepositoryService repositoryService;


    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

//    @Autowired
//    IdentityService identityService;

    @Autowired
    ManagementService managementService;


    /**
     * 发布规则文件
     *
     * @param bpmnName
     */
    @Override
    public void deploy(String bpmnName) {

        String bpmn = "processes/" + bpmnName + ".xml";
        String png = "processes/" + bpmnName + ".png";


        log.debug(String.format("xml:%s,png", bpmn, png));
        //创建一个部署对象
        repositoryService.createDeployment()
                //添加部署的名称
                .name(bpmnName)
                .addInputStream(bpmn, this.getClass().getClassLoader().getResourceAsStream(bpmn))
//                .addInputStream(png, this.getClass().getClassLoader().getResourceAsStream(png))
                .deploy();//完成部署
    }


    /**
     * 发布规则文件
     *
     * @param fileName
     */
    @Override
    public void deploy(InputStream fileInputStream, String fileName) {

        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        //使用deploy方法发布流程
        repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .name(fileName == null ? "" : fileName)
                .deploy();
    }


    @Override
    public ProcessInstance startProcess(String instanceKey, String assignee, Map variables) {
        /**
         * 启动请假单流程  并获取流程实例
         * 因为该请假单流程可以会启动多个所以每启动一个请假单流程都会在数据库中插入一条新版本的流程数据
         * 通过key启动的流程就是当前key下最新版本的流程
         *
         */
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey, variables);
        log.debug(String.format("id:%s,activitiId:%s", processInstance.getId(), processInstance.getActivityId()));

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.setAssignee(task.getId(), assignee);
        return processInstance;
    }

    @Override
    public List queryProcess(String instanceKey) {

        //创建查询对象
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

        ProcessDefinitionQuery processDefinitionQuery = query.latestVersion();
        if (StringUtils.isBlank(instanceKey)) {
            processDefinitionQuery.list();
        } else {
            query.processDefinitionKey(instanceKey);
        }

        //添加查询条件
        // .processDefinitionName("My process")//通过name获取
        // .orderByProcessDefinitionId()//根据ID排序
        //执行查询获取流程定义明细
        List<ProcessDefinition> pds = query.list();
        log.debug("queryProcdef query list:" + pds);
        if (pds != null && pds.size() > 0) {
            for (ProcessDefinition pd : pds) {
                log.debug("ID:" + pd.getId() + ",NAME:" + pd.getName() + ",KEY:" + pd.getKey() + ",VERSION:" + pd.getVersion() + ",RESOURCE_NAME:" + pd.getResourceName() + ",DGRM_RESOURCE_NAME:" + pd.getDiagramResourceName());
            }
        }


        return listToBeanVo(pds, ProcessDefinitionVo.class);
    }

    @Override
    public List queryTask(String assignee, String candidateUser, String candidateGroup, int firstResult, int maxResults) {

        //获取任务服务对象
        //根据接受人获取该用户的任务

        TaskQuery taskQuery = taskService.createTaskQuery();

        if (!StringUtils.isBlank(assignee)) {
            taskQuery.taskAssignee(assignee);
        }

        if (!StringUtils.isBlank(candidateUser)) {
            taskQuery.taskCandidateUser(candidateUser);
        }
        if (!StringUtils.isBlank(candidateGroup)) {
            taskQuery.taskCandidateGroup(candidateGroup);
        }

        List<Task> tasks = taskQuery.listPage(firstResult, maxResults);


        List<TaskVo> list1 = null;
        if (tasks != null && tasks.size() > 0) {

            list1 = listToBeanVo(tasks, TaskVo.class, "variables");

            for (TaskVo task : list1) {

                Map<String, Object> variables = taskService.getVariables(task.getId());
                task.setVariables(variables);

                log.debug("ID:" + task.getId() + ",姓名:" + task.getName() + ",接收人:" + task.getAssignee() + ",开始时间:" + task.getCreateTime());
            }
        }


        return list1;
    }


    @Override
    public Map<String, Object> queryVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    @Override
    public List queryTaskHistory(String processInstanceId) {

        List<HistoricTaskInstance> list = historyService
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .processInstanceId(processInstanceId)//
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();


        List<HistoricTaskInstanceVo> list1 = null;
        if (list != null && list.size() > 0) {
            list1 = new ArrayList<>();
            for (HistoricTaskInstance hti : list) {
                log.debug(hti.getId() + "    " + hti.getName() + "    " + hti.getProcessInstanceId() + "   " + hti.getStartTime() + "   " + hti.getEndTime() + "   " + hti.getDurationInMillis());
                log.debug("################################");

                HistoricTaskInstanceVo historicTaskInstanceVo = objToBeanVo(hti, HistoricTaskInstanceVo.class);
                if (historicTaskInstanceVo != null) {

                    List<HistoricVariableInstance> list2 = historyService.createHistoricVariableInstanceQuery().taskId(hti.getId()).list();
                    if (list2 != null && list2.size() > 0) {

                        Map<String, Object> variables = new HashMap<>();
                        for (HistoricVariableInstance historicVariableInstance : list2) {
                            variables.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
                        }
                        historicTaskInstanceVo.setVariables(variables);
                    }


                    list1.add(historicTaskInstanceVo);
                }

            }
        }

        return list1;
    }

    @Override
    public void completeTask(String taskId, String assignee, Map<String, Object> variables, Map<String, Object> param) {

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        //完成请假申请任务
        taskService.setVariablesLocal(taskId, variables);
        if (!StringUtils.isBlank(assignee)) {
            taskService.setAssignee(taskId, assignee);
        }
        taskService.complete(taskId, variables);

        if (task != null) {
            param.put("isFinish", isFinishProcess(task.getProcessInstanceId()));
        }

    }

    @Override
    public void claimTask(String taskId, String assignee) {
        taskService.claim(taskId, assignee);
    }

    @Override
    public void deleteTask(String taskId) {
        taskService.deleteTask(taskId);
    }


    @Override
    public boolean isFinishProcess(String processInstanceId) {

        /**判断流程是否结束，查询正在执行的执行对象表*/
        ProcessInstance rpi = processEngine.getRuntimeService()
                //创建流程实例查询对象
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        return rpi == null;
    }

    @Override
    public List queryWaitTask(int firstResult, int maxResults) {
        List<Task> list = taskService.createTaskQuery().listPage(firstResult, maxResults);
        return listToBeanVo(list, TaskVo.class, "variables");
    }


    @Override
    public void rejectTask(String taskId, String assignee, boolean returnStart) {
        jump(this, taskId, assignee, returnStart);
    }

    @Override
    public InputStream viewProcessImage(String processInstanceId) {
        //logger.info("[开始]-获取流程图图像");
        //  获取历史流程实例
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        if (historicProcessInstance == null) {
            return null;
        }

        // 获取流程定义
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(historicProcessInstance.getProcessDefinitionId());

        // 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
        List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).orderByHistoricActivityInstanceId().asc().list();


        // 已执行的节点ID集合
        List<String> executedActivityIdList = new ArrayList<String>();
        for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
            executedActivityIdList.add(activityInstance.getActivityId());

        }

        BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());


        // 获取流程走过的线 (getHighLightedFlows是下面的方法)
        List<String> flowIds = getHighLightedFlows(bpmnModel, processDefinition, historicActivityInstanceList);


        if (bpmnModel != null && bpmnModel.getLocationMap().size() > 0) {
            ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
            return generator.generateDiagram(bpmnModel, executedActivityIdList, flowIds, "宋体", "微软雅黑", "黑体",true,"processInstanceId.svg");
        }

        throw new RuntimeException("流程实例不存在");
    }

    /****
     *
     * @param bpmnModel
     * @param processDefinitionEntity
     * @param historicActivityInstances
     * @return
     */
    private List<String> getHighLightedFlows(BpmnModel bpmnModel, ProcessDefinitionEntity processDefinitionEntity, List<HistoricActivityInstance> historicActivityInstances) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //24小时制
        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId

        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
            // 对历史流程节点进行遍历
            // 得到节点定义的详细信息
            FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i).getActivityId());


            List<FlowNode> sameStartTimeNodes = new ArrayList<FlowNode>();// 用以保存后续开始时间相同的节点
            FlowNode sameActivityImpl1 = null;

            HistoricActivityInstance activityImpl_ = historicActivityInstances.get(i);// 第一个节点
            HistoricActivityInstance activityImp2_;

            for (int k = i + 1; k <= historicActivityInstances.size() - 1; k++) {
                activityImp2_ = historicActivityInstances.get(k);// 后续第1个节点

                if (activityImpl_.getActivityType().equals("userTask") && activityImp2_.getActivityType().equals("userTask") &&
                        df.format(activityImpl_.getStartTime()).equals(df.format(activityImp2_.getStartTime()))) //都是usertask，且主节点与后续节点的开始时间相同，说明不是真实的后继节点
                {

                } else {
                    sameActivityImpl1 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(k).getActivityId());//找到紧跟在后面的一个节点
                    break;
                }

            }
            sameStartTimeNodes.add(sameActivityImpl1); // 将后面第一个节点放在时间相同节点的集合里
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点

                if (df.format(activityImpl1.getStartTime()).equals(df.format(activityImpl2.getStartTime()))) {// 如果第一个节点和第二个节点开始时间相同保存
                    FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {// 有不相同跳出循环
                    break;
                }
            }
            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows(); // 取出节点的所有出去的线

            for (SequenceFlow pvmTransition : pvmTransitions) {// 对所有的线进行遍历
                FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(pvmTransition.getTargetRef());// 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }

        }
        return highFlows;

    }


    /***
     * 跳转方法
     * @param activitiService
     * @param taskId
     * @param assignee
     * @param returnStart
     */
    private void jump(ActivitiServiceImpl activitiService, String taskId, String assignee, boolean returnStart) {
        //当前任务
        Task currentTask = activitiService.taskService.createTaskQuery().taskId(taskId).singleResult();
        //获取流程定义
        BpmnModel bpmnModel = repositoryService.getBpmnModel(currentTask.getProcessDefinitionId());


        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery().processInstanceId(currentTask.getProcessInstanceId()).activityType("userTask").finished().orderByHistoricActivityInstanceEndTime().asc().list();
        if (list == null || list.size() == 0) {
            throw new ActivitiException("操作历史流程不存在");
        }

        //获取目标节点定义
        FlowNode targetNode = null;

        //驳回到发起点
        if (returnStart) {

            targetNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(list.get(0).getActivityId());
        } else {//驳回到上一个节点

            FlowNode currNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentTask.getTaskDefinitionKey());

            //倒序审核任务列表，最后一个不与当前节点相同的节点设置为目标节点
            for (int i = 0; i < list.size(); i++) {
                FlowNode lastNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(list.get(i).getActivityId());
                if (list.size() > 0 && currNode.getId().equals(lastNode.getId())) {
                    targetNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(list.get(i - 1).getActivityId());
                    break;
                }
            }

            if (targetNode == null && list.size() > 0) {
                targetNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(list.get(list.size() - 1).getActivityId());
            }


//            Map<String, Object> flowElementMap = new TreeMap<>();
//            Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
//            for (FlowElement flowElement : flowElements) {
//
//                flowElementMap.put(flowElement.getId(), flowElement);
//            }
//
//
//
//            targetNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(tmplist.get(tmplist.size() - 1).getActivityId());

        }

        if (targetNode == null) {
            throw new ActivitiException("开始节点不存在");
        }


        //删除当前运行任务
        String executionEntityId = activitiService.managementService.executeCommand(activitiService.new DeleteTaskCmd(currentTask.getId()));
        //流程执行到来源节点
        activitiService.managementService.executeCommand(activitiService.new SetFLowNodeAndGoCmd(targetNode, executionEntityId));
    }


    /***
     *     删除当前运行时任务命令，并返回当前任务的执行对象id
     *      这里继承了NeedsActiveTaskCmd，主要时很多跳转业务场景下，要求不能时挂起任务。可以直接继承Command即可
     */
    public class DeleteTaskCmd extends NeedsActiveTaskCmd<String> {
        public DeleteTaskCmd(String taskId) {
            super(taskId);
        }

        @Override
        public String execute(CommandContext commandContext, TaskEntity currentTask) {
            //获取所需服务
            TaskEntityManagerImpl taskEntityManager = (TaskEntityManagerImpl) commandContext.getTaskEntityManager();
            //获取当前任务的来源任务及来源节点信息
            ExecutionEntity executionEntity = currentTask.getExecution();
            //删除当前任务,来源任务
            taskEntityManager.deleteTask(currentTask, "jumpReason", false, false);
            return executionEntity.getId();
        }

        @Override
        public String getSuspendedTaskException() {
            return "挂起的任务不能跳转";
        }
    }

    /****
     * 根据提供节点和执行对象id，进行跳转命令
     */
    public class SetFLowNodeAndGoCmd implements Command<Void> {
        private FlowNode flowElement;
        private String executionId;

        public SetFLowNodeAndGoCmd(FlowNode flowElement, String executionId) {
            this.flowElement = flowElement;
            this.executionId = executionId;
        }

        @Override
        public Void execute(CommandContext commandContext) {

            ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findById(executionId);

            //获取目标节点的来源连线
            List<SequenceFlow> flows = flowElement.getIncomingFlows();
            if (flows == null || flows.size() < 1) {

                executionEntity.setCurrentFlowElement(flowElement);
                commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(executionEntity, true);

            } else {
                //随便选一条连线来执行，时当前执行计划为，从连线流转到目标节点，实现跳转
                executionEntity.setCurrentFlowElement(flows.get(0));
            }

            commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(executionEntity, true);

            return null;
        }
    }


    /***
     * 转化显示Bean
     * @param list 待转化列表
     * @param clazz 显示类
     * @param <T>
     * @return
     * @throws Exception
     */
    private <T> List<T> listToBeanVo(List list, Class<T> clazz, String... ignoreProperties) {
        if (list == null) {
            return null;
        }

        List<T> rlist = new ArrayList<>();
        try {
            for (Object obj : list) {
                T t = objToBeanVo(obj, clazz, ignoreProperties);
                rlist.add(t);
            }
        } catch (Exception e) {
            log.error("listToBeanVo error:" + e.getMessage());
            e.printStackTrace();
        }
        return rlist;
    }

    /**
     * 复制源对象属性到目标对象
     *
     * @param obj
     * @param clazz
     * @param ignoreProperties
     * @param <T>
     * @return
     * @throws Exception
     */
    private <T> T objToBeanVo(Object obj, Class<T> clazz, String... ignoreProperties) {
        try {
            T t = (T) Class.forName(clazz.getName()).newInstance();
            BeanUtils.copyProperties(obj, t, ignoreProperties);
            return t;
        } catch (Exception e) {
            log.error("objToBeanVo error:" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
