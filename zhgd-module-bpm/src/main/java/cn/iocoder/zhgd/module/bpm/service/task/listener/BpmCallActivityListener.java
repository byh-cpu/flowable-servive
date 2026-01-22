package cn.iocoder.zhgd.module.bpm.service.task.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.zhgd.framework.common.util.json.JsonUtils;
import cn.iocoder.zhgd.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.zhgd.module.bpm.enums.definition.BpmChildProcessStartUserEmptyTypeEnum;
import cn.iocoder.zhgd.module.bpm.enums.definition.BpmChildProcessStartUserTypeEnum;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.zhgd.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * BPM 子流程监听器：设置流程的发起人
 *
 * @author Lesan
 */
@Component
@Slf4j
public class BpmCallActivityListener implements ExecutionListener {

    public static final String DELEGATE_EXPRESSION = "${bpmCallActivityListener}";

    @Setter
    private Expression listenerConfig;

    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public void notify(DelegateExecution execution) {
        String expressionText = listenerConfig.getExpressionText();
        Assert.notNull(expressionText, "监听器扩展字段({})不能为空", expressionText);
        BpmSimpleModelNodeVO.ChildProcessSetting.StartUserSetting startUserSetting = JsonUtils.parseObject(
                expressionText, BpmSimpleModelNodeVO.ChildProcessSetting.StartUserSetting.class);
        ProcessInstance processInstance = processInstanceService.getProcessInstance(execution.getRootProcessInstanceId());

        // 1. 当发起人来源为主流程发起人时，并兜底 startUserSetting 为空时
        if (startUserSetting == null
                || startUserSetting.getType().equals(BpmChildProcessStartUserTypeEnum.MAIN_PROCESS_START_USER.getType())) {
            FlowableUtils.setAuthenticatedUserId(processInstance.getStartUserId());
            return;
        }

        // 2. 当发起人来源为表单时
        if (startUserSetting.getType().equals(BpmChildProcessStartUserTypeEnum.FROM_FORM.getType())) {
            String formFieldValue = MapUtil.getStr(processInstance.getProcessVariables(), startUserSetting.getFormField());
            // 2.1 当表单值为空时
            if (StrUtil.isEmpty(formFieldValue)) {
                // 2.1.1 来自主流程发起人
                if (startUserSetting.getEmptyType().equals(BpmChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_START_USER.getType())) {
                    FlowableUtils.setAuthenticatedUserId(processInstance.getStartUserId());
                    return;
                }
                // 2.1.2 来自子流程管理员
                if (startUserSetting.getEmptyType().equals(BpmChildProcessStartUserEmptyTypeEnum.CHILD_PROCESS_ADMIN.getType())) {
                    BpmProcessDefinitionInfoDO processDefinition = processDefinitionService.getProcessDefinitionInfo(execution.getProcessDefinitionId());
                    List<String> managerUserIds = processDefinition.getManagerUserIds();
                    FlowableUtils.setAuthenticatedUserId(CollUtil.getFirst(managerUserIds));
                    return;
                }
                // 2.1.3 来自主流程管理员
                if (startUserSetting.getEmptyType().equals(BpmChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_ADMIN.getType())) {
                    BpmProcessDefinitionInfoDO processDefinition = processDefinitionService.getProcessDefinitionInfo(processInstance.getProcessDefinitionId());
                    List<String> managerUserIds = processDefinition.getManagerUserIds();
                    FlowableUtils.setAuthenticatedUserId(CollUtil.getFirst(managerUserIds));
                    return;
                }
            }
            // 2.2 使用表单值，并兜底异常情况
            try {
                List<String> formFieldValues = JsonUtils.parseArray(formFieldValue, String.class);
                if (CollUtil.isNotEmpty(formFieldValues)) {
                    FlowableUtils.setAuthenticatedUserId(formFieldValues.get(0));
                    return;
                }
            } catch (Exception ex) {
                log.debug("[notify][监听器：{}，子流程监听器表单值解析失败，value：{}]",
                        DELEGATE_EXPRESSION, formFieldValue);
            }
            FlowableUtils.setAuthenticatedUserId(StrUtil.isNotEmpty(formFieldValue)
                    ? formFieldValue : processInstance.getStartUserId());
        }
    }

}