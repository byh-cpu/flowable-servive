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

import static cn.iocoder.zhgd.framework.common.util.collection.SetUtils.asSet;

/**
 * 发起人的部门负责人, 可以是上级部门负责人 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author jason
 */
@Component
public class BpmTaskCandidateStartUserDeptLeaderStrategy extends AbstractBpmTaskCandidateDeptLeaderStrategy {

    @Resource
    @Lazy // 避免循环依赖
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.START_USER_DEPT_LEADER;
    }

    @Override
    public void validateParam(String param) {
        // 参数是部门的层级
        Assert.isTrue(Integer.parseInt(param) > 0, "部门的层级必须大于 0");
    }

    @Override
    public Set<String> calculateUsersByTask(DelegateExecution execution, String param) {
        // 获得流程发起人
        ProcessInstance processInstance = processInstanceService.getProcessInstance(execution.getProcessInstanceId());
        // 获取发起人的部门负责人
        return getStartUserDeptLeader(processInstance.getStartUserId(), param);
    }

    @Override
    public Set<String> calculateUsersByActivity(BpmnModel bpmnModel, String activityId, String param,
                                                String startUserId, String processDefinitionId, Map<String, Object> processVariables) {
        // 获取发起人的部门负责人
        return getStartUserDeptLeader(startUserId, param);
    }

    private Set<String> getStartUserDeptLeader(String startUserId, String param) {
        int level = Integer.parseInt(param); // 参数是部门的层级
        DeptRespDTO dept = super.getStartUserDept(startUserId);
        if (dept == null) {
            return new HashSet<>();
        }
        Long deptLeaderId = super.getAssignLevelDeptLeaderId(dept, level);
        return deptLeaderId != null ? asSet(String.valueOf(deptLeaderId)) : new HashSet<>();
    }

}
