package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.framework.common.pojo.PageResult;
import cn.iocoder.zhgd.framework.common.util.date.DateUtils;
import cn.iocoder.zhgd.framework.common.util.object.BeanUtils;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskPageReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskSimpleRespDTO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskPageReqVO;
import cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.zhgd.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.zhgd.module.bpm.service.task.BpmTaskService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 流程任务查询 Api 实现类
 */
@Service
@DubboService
public class BpmTaskQueryApiImpl implements BpmTaskQueryApi {

    @Resource
    private BpmTaskService taskService;
    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Override
    public PageResult<BpmTaskSimpleRespDTO> getTodoTaskPage(String userId, BpmTaskPageReqDTO pageReqDTO) {
        BpmTaskPageReqVO pageReqVO = BeanUtils.toBean(pageReqDTO, BpmTaskPageReqVO.class);
        PageResult<Task> pageResult = taskService.getTaskTodoPage(userId, pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty();
        }
        Map<String, ProcessInstance> processInstanceMap = processInstanceService.getProcessInstanceMap(
                convertSet(pageResult.getList(), Task::getProcessInstanceId));
        Map<String, ProcessDefinition> definitionMap = processDefinitionService.getProcessDefinitionMap(
                convertSet(pageResult.getList(), Task::getProcessDefinitionId));
        Map<String, BpmProcessDefinitionInfoDO> definitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), Task::getProcessDefinitionId));
        return buildTaskPage(pageResult, processInstanceMap, null, definitionMap, definitionInfoMap);
    }

    @Override
    public PageResult<BpmTaskSimpleRespDTO> getDoneTaskPage(String userId, BpmTaskPageReqDTO pageReqDTO) {
        BpmTaskPageReqVO pageReqVO = BeanUtils.toBean(pageReqDTO, BpmTaskPageReqVO.class);
        PageResult<HistoricTaskInstance> pageResult = taskService.getTaskDonePage(userId, pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty();
        }
        Map<String, HistoricProcessInstance> processInstanceMap = processInstanceService.getHistoricProcessInstanceMap(
                convertSet(pageResult.getList(), HistoricTaskInstance::getProcessInstanceId));
        Map<String, ProcessDefinition> definitionMap = processDefinitionService.getProcessDefinitionMap(
                convertSet(pageResult.getList(), HistoricTaskInstance::getProcessDefinitionId));
        Map<String, BpmProcessDefinitionInfoDO> definitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), HistoricTaskInstance::getProcessDefinitionId));
        return buildTaskPage(pageResult, null, processInstanceMap, definitionMap, definitionInfoMap);
    }

    @Override
    public PageResult<BpmTaskSimpleRespDTO> getManagerTaskPage(String userId, BpmTaskPageReqDTO pageReqDTO) {
        BpmTaskPageReqVO pageReqVO = BeanUtils.toBean(pageReqDTO, BpmTaskPageReqVO.class);
        PageResult<HistoricTaskInstance> pageResult = taskService.getTaskPage(userId, pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty();
        }
        Map<String, HistoricProcessInstance> processInstanceMap = processInstanceService.getHistoricProcessInstanceMap(
                convertSet(pageResult.getList(), HistoricTaskInstance::getProcessInstanceId));
        Map<String, ProcessDefinition> definitionMap = processDefinitionService.getProcessDefinitionMap(
                convertSet(pageResult.getList(), HistoricTaskInstance::getProcessDefinitionId));
        Map<String, BpmProcessDefinitionInfoDO> definitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), HistoricTaskInstance::getProcessDefinitionId));
        return buildTaskPage(pageResult, null, processInstanceMap, definitionMap, definitionInfoMap);
    }

    private PageResult<BpmTaskSimpleRespDTO> buildTaskPage(PageResult<? extends TaskInfo> pageResult,
                                                          Map<String, ProcessInstance> processInstanceMap,
                                                          Map<String, HistoricProcessInstance> historicProcessInstanceMap,
                                                          Map<String, ProcessDefinition> definitionMap,
                                                          Map<String, BpmProcessDefinitionInfoDO> definitionInfoMap) {
        List<BpmTaskSimpleRespDTO> list = convertList(pageResult.getList(),
                task -> buildTaskResp(task, processInstanceMap, historicProcessInstanceMap, definitionMap, definitionInfoMap));
        return new PageResult<>(list, pageResult.getTotal());
    }

    private BpmTaskSimpleRespDTO buildTaskResp(TaskInfo task,
                                               Map<String, ProcessInstance> processInstanceMap,
                                               Map<String, HistoricProcessInstance> historicProcessInstanceMap,
                                               Map<String, ProcessDefinition> definitionMap,
                                               Map<String, BpmProcessDefinitionInfoDO> definitionInfoMap) {
        BpmTaskSimpleRespDTO resp = new BpmTaskSimpleRespDTO();
        resp.setId(task.getId());
        resp.setName(task.getName());
        resp.setTaskDefinitionKey(task.getTaskDefinitionKey());
        resp.setStatus(FlowableUtils.getTaskStatus(task));
        resp.setReason(FlowableUtils.getTaskReason(task));
        resp.setCreateTime(DateUtils.of(task.getCreateTime()));
        if (task instanceof HistoricTaskInstance) {
            resp.setEndTime(DateUtils.of(((HistoricTaskInstance) task).getEndTime()));
        }
        resp.setProcessInstanceId(task.getProcessInstanceId());
        resp.setProcessDefinitionId(task.getProcessDefinitionId());
        resp.setAssigneeUserId(task.getAssignee());
        resp.setOwnerUserId(task.getOwner());
        Map<String, Object> formVariables = FlowableUtils.getTaskFormVariable(task);
        if (CollUtil.isEmpty(formVariables)) {
            Map<String, Object> processVariables = null;
            if (task instanceof Task) {
                processVariables = ((Task) task).getProcessVariables();
            }
            if (processVariables == null && instance != null) {
                processVariables = instance.getProcessVariables();
            }
            if (processVariables == null && historicInstance != null) {
                processVariables = historicInstance.getProcessVariables();
            }
            if (processVariables != null) {
                formVariables = FlowableUtils.filterProcessInstanceFormVariable(new java.util.HashMap<>(processVariables));
            }
        }
        resp.setFormVariables(formVariables);

        ProcessDefinition definition = definitionMap.get(task.getProcessDefinitionId());
        if (definition != null) {
            resp.setProcessDefinitionKey(definition.getKey());
        }
        BpmProcessDefinitionInfoDO info = definitionInfoMap.get(task.getProcessDefinitionId());
        if (info != null) {
            resp.setCategory(info.getCategory());
        } else if (definition != null) {
            resp.setCategory(definition.getCategory());
        }

        ProcessInstance instance = processInstanceMap != null ? processInstanceMap.get(task.getProcessInstanceId()) : null;
        if (instance != null) {
            resp.setProcessInstanceName(instance.getName());
            resp.setStartUserId(instance.getStartUserId());
        }
        HistoricProcessInstance historicInstance = historicProcessInstanceMap != null
                ? historicProcessInstanceMap.get(task.getProcessInstanceId()) : null;
        if (historicInstance != null) {
            resp.setProcessInstanceName(historicInstance.getName());
            resp.setStartUserId(historicInstance.getStartUserId());
        }
        return resp;
    }

}
