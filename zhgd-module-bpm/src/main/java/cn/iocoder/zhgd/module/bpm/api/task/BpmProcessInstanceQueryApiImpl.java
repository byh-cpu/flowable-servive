package cn.iocoder.zhgd.module.bpm.api.task;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.zhgd.framework.common.pojo.PageResult;
import cn.iocoder.zhgd.framework.common.util.date.DateUtils;
import cn.iocoder.zhgd.framework.common.util.object.BeanUtils;
import cn.iocoder.zhgd.framework.common.util.string.StrUtils;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceLiteRespDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstancePageReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceSimpleRespDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceTaskDetailRespDTO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.instance.BpmProcessInstancePageReqVO;
import cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.zhgd.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.zhgd.module.bpm.service.task.BpmTaskService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 流程实例查询 Api 实现类
 */
@Service
@DubboService
public class BpmProcessInstanceQueryApiImpl implements BpmProcessInstanceQueryApi {

    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmTaskService taskService;
    @Resource
    private BpmTaskCandidateInvoker taskCandidateInvoker;

    @Override
    public PageResult<BpmProcessInstanceSimpleRespDTO> getMyProcessInstancePage(String userId,
                                                                              BpmProcessInstancePageReqDTO pageReqDTO) {
        BpmProcessInstancePageReqVO pageReqVO = BeanUtils.toBean(pageReqDTO, BpmProcessInstancePageReqVO.class);
        PageResult<HistoricProcessInstance> pageResult = processInstanceService.getProcessInstancePage(userId, pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty();
        }
        Map<String, ProcessDefinition> definitionMap = processDefinitionService.getProcessDefinitionMap(
                convertSet(pageResult.getList(), HistoricProcessInstance::getProcessDefinitionId));
        Map<String, BpmProcessDefinitionInfoDO> definitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), HistoricProcessInstance::getProcessDefinitionId));
        List<BpmProcessInstanceSimpleRespDTO> list = convertList(pageResult.getList(),
                instance -> buildProcessInstanceResp(instance, definitionMap, definitionInfoMap));
        return new PageResult<>(list, pageResult.getTotal());
    }

    private BpmProcessInstanceSimpleRespDTO buildProcessInstanceResp(HistoricProcessInstance instance,
                                                                     Map<String, ProcessDefinition> definitionMap,
                                                                     Map<String, BpmProcessDefinitionInfoDO> definitionInfoMap) {
        BpmProcessInstanceSimpleRespDTO resp = new BpmProcessInstanceSimpleRespDTO();
        resp.setId(instance.getId());
        resp.setName(instance.getName());
        resp.setTitle(FlowableUtils.getProcessInstanceTitle(instance));
        resp.setStartTime(DateUtils.of(instance.getStartTime()));
        resp.setEndTime(DateUtils.of(instance.getEndTime()));
        resp.setStatus(FlowableUtils.getProcessInstanceStatus(instance));
        resp.setProcessDefinitionId(instance.getProcessDefinitionId());
        resp.setStartUserId(instance.getStartUserId());
        resp.setFormVariables(FlowableUtils.getProcessInstanceFormVariable(instance));

        ProcessDefinition definition = definitionMap.get(instance.getProcessDefinitionId());
        if (definition != null) {
            resp.setProcessDefinitionKey(definition.getKey());
        }
        BpmProcessDefinitionInfoDO info = definitionInfoMap.get(instance.getProcessDefinitionId());
        if (info != null) {
            resp.setCategory(info.getCategory());
        } else if (definition != null) {
            resp.setCategory(definition.getCategory());
        }
        return resp;
    }

    @Override
    public BpmProcessInstanceLiteRespDTO getProcessInstanceDetailLite(String processInstanceId) {
        HistoricProcessInstance instance = processInstanceService.getHistoricProcessInstance(processInstanceId);
        if (instance == null) {
            return null;
        }

        return buildProcessInstanceLite(instance);
    }

    @Override
    public BpmProcessInstanceTaskDetailRespDTO getProcessInstanceDetail(String processInstanceId) {
        HistoricProcessInstance instance = processInstanceService.getHistoricProcessInstance(processInstanceId);
        if (instance == null) {
            return null;
        }

        BpmProcessInstanceTaskDetailRespDTO resp = new BpmProcessInstanceTaskDetailRespDTO();
        resp.setProcessInstance(buildProcessInstanceLite(instance));
        resp.setFormVariables(FlowableUtils.getProcessInstanceFormVariable(instance));

        BpmnModel bpmnModel = processDefinitionService.getProcessDefinitionBpmnModel(instance.getProcessDefinitionId());
        List<BpmProcessInstanceTaskDetailRespDTO.TaskDetail> tasks = new ArrayList<>();
        taskService.getTaskListByProcessInstanceId(processInstanceId, Boolean.TRUE).forEach(task -> {
            BpmProcessInstanceTaskDetailRespDTO.TaskDetail taskDetail = new BpmProcessInstanceTaskDetailRespDTO.TaskDetail();
            taskDetail.setId(task.getId());
            taskDetail.setName(task.getName());
            taskDetail.setTaskDefinitionKey(task.getTaskDefinitionKey());
            taskDetail.setStatus(FlowableUtils.getTaskStatus(task));
            taskDetail.setReason(FlowableUtils.getTaskReason(task));
            taskDetail.setCreateTime(DateUtils.of(task.getCreateTime()));
            taskDetail.setEndTime(DateUtils.of(task.getEndTime()));
            taskDetail.setAssigneeUserId(task.getAssignee());
            taskDetail.setOwnerUserId(task.getOwner());
            taskDetail.setTaskVariables(FlowableUtils.getTaskFormVariable(task));

            fillTaskCandidates(taskDetail, bpmnModel, instance);
            tasks.add(taskDetail);
        });
        resp.setTasks(tasks);
        return resp;
    }

    @Override
    public List<String> getRunningTaskIds(String processInstanceId) {
        List<Task> tasks = taskService.getRunningTaskListByProcessInstanceId(processInstanceId, null, null);
        return convertList(tasks, Task::getId);
    }

    private BpmProcessInstanceLiteRespDTO buildProcessInstanceLite(HistoricProcessInstance instance) {
        BpmProcessInstanceLiteRespDTO resp = new BpmProcessInstanceLiteRespDTO();
        resp.setId(instance.getId());
        resp.setName(instance.getName());
        resp.setTitle(FlowableUtils.getProcessInstanceTitle(instance));
        resp.setBusinessKey(instance.getBusinessKey());
        resp.setStartTime(DateUtils.of(instance.getStartTime()));
        resp.setEndTime(DateUtils.of(instance.getEndTime()));
        resp.setStatus(FlowableUtils.getProcessInstanceStatus(instance));
        resp.setProcessDefinitionId(instance.getProcessDefinitionId());
        resp.setStartUserId(instance.getStartUserId());
        resp.setFormVariables(FlowableUtils.getProcessInstanceFormVariable(instance));

        ProcessDefinition definition = processDefinitionService.getProcessDefinition(instance.getProcessDefinitionId());
        if (definition != null) {
            resp.setProcessDefinitionKey(definition.getKey());
        }
        BpmProcessDefinitionInfoDO info = processDefinitionService.getProcessDefinitionInfo(instance.getProcessDefinitionId());
        if (info != null) {
            resp.setCategory(info.getCategory());
        } else if (definition != null) {
            resp.setCategory(definition.getCategory());
        }
        return resp;
    }

    private void fillTaskCandidates(BpmProcessInstanceTaskDetailRespDTO.TaskDetail taskDetail,
                                    BpmnModel bpmnModel,
                                    HistoricProcessInstance instance) {
        if (bpmnModel == null || StrUtil.isEmpty(taskDetail.getTaskDefinitionKey())) {
            return;
        }
        FlowElement flowElement = BpmnModelUtils.getFlowElementById(bpmnModel, taskDetail.getTaskDefinitionKey());
        Integer strategy = BpmnModelUtils.parseCandidateStrategy(flowElement);
        taskDetail.setCandidateStrategy(strategy);
        if (strategy == null) {
            return;
        }
        String param = BpmnModelUtils.parseCandidateParam(flowElement);
        if (BpmTaskCandidateStrategyEnum.ROLE.getStrategy().equals(strategy)) {
            if (StrUtil.isNotEmpty(param)) {
                taskDetail.setCandidateRoleIds(convertList(StrUtils.splitToLongSet(param), String::valueOf));
            }
            return;
        }
        List<String> userIds = new ArrayList<>(taskCandidateInvoker.calculateUsersByActivity(
                bpmnModel, taskDetail.getTaskDefinitionKey(), instance.getStartUserId(),
                instance.getProcessDefinitionId(), instance.getProcessVariables()));
        taskDetail.setCandidateUserIds(userIds);
    }

}
