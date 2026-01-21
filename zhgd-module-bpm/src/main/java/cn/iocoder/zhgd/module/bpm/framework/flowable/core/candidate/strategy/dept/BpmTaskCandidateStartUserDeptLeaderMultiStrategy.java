package cn.iocoder.zhgd.module.bpm.framework.flowable.core.candidate.strategy.dept;

import cn.hutool.core.lang.Assert;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.zhgd.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.zhgd.module.system.api.dept.dto.DeptRespDTO;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.collection.ListUtil.toList;

/**
 * 发起人连续多级部门的负责人 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author jason
 */
@Component
public class BpmTaskCandidateStartUserDeptLeaderMultiStrategy extends AbstractBpmTaskCandidateDeptLeaderStrategy {

    @Resource
    @Lazy
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.START_USER_DEPT_LEADER_MULTI;
    }

    @Override
    public void validateParam(String param) {
        int level = Integer.parseInt(param); // 参数是部门的层级
        Assert.isTrue(level > 0, "部门的层级必须大于 0");
    }

    @Override
    public Set<String> calculateUsersByTask(DelegateExecution execution, String param) {
        int level = Integer.parseInt(param); // 参数是部门的层级
        // 获得流程发起人
        ProcessInstance processInstance = processInstanceService.getProcessInstance(execution.getProcessInstanceId());
        // 获取发起人的 multi 部门负责人
        DeptRespDTO dept = super.getStartUserDept(processInstance.getStartUserId());
        if (dept == null) {
            return new HashSet<>();
        }
        return cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.convertSet(
                super.getMultiLevelDeptLeaderIds(toList(dept.getId()), level), String::valueOf);
    }

    @Override
    public Set<String> calculateUsersByActivity(BpmnModel bpmnModel, String activityId, String param,
                                                String startUserId, String processDefinitionId, Map<String, Object> processVariables) {
        int level = Integer.parseInt(param); // 参数是部门的层级
        DeptRespDTO dept = super.getStartUserDept(startUserId);
        if (dept == null) {
            return new HashSet<>();
        }
        return cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.convertSet(
                super.getMultiLevelDeptLeaderIds(toList(dept.getId()), level), String::valueOf);
    }

}
