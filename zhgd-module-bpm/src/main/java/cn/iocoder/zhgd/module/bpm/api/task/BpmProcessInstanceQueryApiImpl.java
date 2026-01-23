package cn.iocoder.zhgd.module.bpm.api.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.zhgd.framework.common.pojo.PageResult;
import cn.iocoder.zhgd.framework.common.util.date.DateUtils;
import cn.iocoder.zhgd.framework.common.util.object.BeanUtils;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceDetailRespDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstancePageReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceSimpleRespDTO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailReqVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.instance.BpmProcessInstancePageReqVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.zhgd.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    public BpmProcessInstanceDetailRespDTO getProcessInstanceDetail(String processInstanceId) {
        HistoricProcessInstance instance = processInstanceService.getHistoricProcessInstance(processInstanceId);
        if (instance == null) {
            return null;
        }

        BpmProcessInstanceDetailRespDTO resp = new BpmProcessInstanceDetailRespDTO();
        resp.setId(instance.getId());
        resp.setName(instance.getName());
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

        resp.setApproverUserIds(new ArrayList<>(getProcessInstanceApproverUserIds(instance)));
        return resp;
    }

    private LinkedHashSet<String> getProcessInstanceApproverUserIds(HistoricProcessInstance instance) {
        LinkedHashSet<String> approverUserIdSet = new LinkedHashSet<>();
        BpmApprovalDetailRespVO detail = processInstanceService.getApprovalDetail(instance.getStartUserId(),
                new BpmApprovalDetailReqVO().setProcessInstanceId(instance.getId()));
        if (detail == null) {
            return approverUserIdSet;
        }

        if (CollUtil.isNotEmpty(detail.getActivityNodes())) {
            detail.getActivityNodes().forEach(node -> {
                if (CollUtil.isNotEmpty(node.getCandidateUserIds())) {
                    approverUserIdSet.addAll(node.getCandidateUserIds());
                }
                if (CollUtil.isNotEmpty(node.getTasks())) {
                    node.getTasks().forEach(task -> addTaskApproverUserIds(approverUserIdSet, task));
                }
            });
        }

        addTodoTaskApproverUserIds(approverUserIdSet, detail.getTodoTask());
        return approverUserIdSet;
    }

    private void addTodoTaskApproverUserIds(LinkedHashSet<String> approverUserIdSet, BpmTaskRespVO task) {
        if (task == null) {
            return;
        }
        addTaskApproverUserIds(approverUserIdSet, task);
        if (CollUtil.isNotEmpty(task.getChildren())) {
            task.getChildren().forEach(child -> addTaskApproverUserIds(approverUserIdSet, child));
        }
    }

    private void addTaskApproverUserIds(LinkedHashSet<String> approverUserIdSet,
                                        BpmApprovalDetailRespVO.ActivityNodeTask task) {
        if (task == null) {
            return;
        }
        if (StrUtil.isNotEmpty(task.getAssignee())) {
            approverUserIdSet.add(task.getAssignee());
        }
        if (StrUtil.isNotEmpty(task.getOwner())) {
            approverUserIdSet.add(task.getOwner());
        }
    }

    private void addTaskApproverUserIds(LinkedHashSet<String> approverUserIdSet, BpmTaskRespVO task) {
        if (task == null) {
            return;
        }
        if (StrUtil.isNotEmpty(task.getAssignee())) {
            approverUserIdSet.add(task.getAssignee());
        }
        if (StrUtil.isNotEmpty(task.getOwner())) {
            approverUserIdSet.add(task.getOwner());
        }
    }

}
