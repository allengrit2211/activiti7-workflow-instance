# Activiti7 工作流引擎例子


###集成Activiti7
###集成bpmn-js 
### 创建数据库  activiti 表会自动创建
### 定义了一些常用API 
### 默认首页 http://localhost:20021/ bpmn-js 界面
### http://localhost:20021/swagger-ui.html swagger-ui地址







### 表说明 

3.2 Activiti 数据库表
创建 ProcessEngine 对象后，会根据配置决定是否创建表。如果需要创建表，会创建以 act_ 开头的25张表

其中表的命名规则为：以 act_ 开头，第二部分是表示表的用途的两个字母标识。用途也和服务的 API 对应

3.2.1 act_re_* 开头的表
re 表示 repository，这个前缀的表包含了流程定义和流程静态资源（图片、规则、等等）

表名	描述
act_re_deployment	流程定义部署表
act_re_model	流程定义模型表
act_re_procdef	流程定义表
3.2.2 act_ru_* 开头的表
ru 表示 runtime，这些运行时的表，包含流程实例、任务、变量、异步任务等运行中的数据。Activiti 只在流程实例执行过程中保存这些数据，在流程结束时就会删除这些记录。这样运行时表可以一直很小速度很快

表名	描述
act_ru_deadletter_job	无法执行的工作表
act_ru_event_subscr	事件描述表
act_ru_execution	执行表，保存流程实例和执行数据
act_ru_identitylink	流程与身份的关系表 ，如用户与任务的关系
act_ru_integration	
act_ru_job	一般的工作表
act_ru_suspended_job	中断的工作表
act_ru_task	任务表
act_ru_timer_job	定时器工作表
act_ru_variable	参数表，用于任务参数，流程参数
3.2.3 act_hi_* 开头的表
hi 表示 history，这些表包含历史数据，比如历史流程实例、变量、任务等等

表名	描述
act_hi_actinst	历史流程实例活动表
act_hi_attachment	流程附件表
act_hi_comment	流程评论表
act_hi_detail	变更历史表
act_hi_identitylink	历史参与者表
act_hi_procinst	流程实例的历史数据表
act_hi_taskinst	流程实例历史任务表
act_hi_varinst	历史参数表
3.2.4 act_ge_* 开头的表
ge 表示 general，通用数据，用于不同场景下

表名	描述
act_ge_bytearray	bpmn、png 等二进制内容
act_ge_property	引擎版本信息
3.2.5 其他表
表名	描述
act_evt_log	事件日志
act_procdef_info	流程定义信息
