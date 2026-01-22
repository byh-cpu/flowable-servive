package cn.iocoder.zhgd.module.bpm.service.message;

import cn.iocoder.zhgd.framework.web.config.WebProperties;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.zhgd.module.bpm.convert.message.BpmMessageConvert;
import cn.iocoder.zhgd.module.bpm.enums.message.BpmMessageEnum;
import cn.iocoder.zhgd.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import cn.iocoder.zhgd.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceRejectReqDTO;
import cn.iocoder.zhgd.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.iocoder.zhgd.module.bpm.service.message.dto.BpmMessageSendWhenTaskTimeoutReqDTO;
import cn.iocoder.zhgd.module.system.api.sms.SmsSendApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * BPM 消息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class BpmMessageServiceImpl implements BpmMessageService {

    @Resource
    private SmsSendApi smsSendApi;

    @Resource
    private WebProperties webProperties;

    @Override
    public void sendMessageWhenProcessInstanceApprove(BpmMessageSendWhenProcessInstanceApproveReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        Long startUserId = NumberUtil.parseLong(reqDTO.getStartUserId(), null);
        if (startUserId == null) {
            log.debug("[sendMessageWhenProcessInstanceApprove][startUserId({}) 非数字，跳过短信通知]", reqDTO.getStartUserId());
            return;
        }
        smsSendApi.sendSingleSmsToAdmin(BpmMessageConvert.INSTANCE.convert(startUserId,
                BpmMessageEnum.PROCESS_INSTANCE_APPROVE.getSmsTemplateCode(), templateParams));
    }

    @Override
    public void sendMessageWhenProcessInstanceReject(BpmMessageSendWhenProcessInstanceRejectReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("reason", reqDTO.getReason());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        Long startUserId = NumberUtil.parseLong(reqDTO.getStartUserId(), null);
        if (startUserId == null) {
            log.debug("[sendMessageWhenProcessInstanceReject][startUserId({}) 非数字，跳过短信通知]", reqDTO.getStartUserId());
            return;
        }
        smsSendApi.sendSingleSmsToAdmin(BpmMessageConvert.INSTANCE.convert(startUserId,
                BpmMessageEnum.PROCESS_INSTANCE_REJECT.getSmsTemplateCode(), templateParams));
    }

    @Override
    public void sendMessageWhenTaskAssigned(BpmMessageSendWhenTaskCreatedReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("taskName", reqDTO.getTaskName());
        templateParams.put("startUserNickname", reqDTO.getStartUserNickname());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        Long assigneeUserId = NumberUtil.parseLong(reqDTO.getAssigneeUserId(), null);
        if (assigneeUserId == null) {
            log.debug("[sendMessageWhenTaskAssigned][assigneeUserId({}) 非数字，跳过短信通知]", reqDTO.getAssigneeUserId());
            return;
        }
        smsSendApi.sendSingleSmsToAdmin(BpmMessageConvert.INSTANCE.convert(assigneeUserId,
                BpmMessageEnum.TASK_ASSIGNED.getSmsTemplateCode(), templateParams));
    }

    @Override
    public void sendMessageWhenTaskTimeout(BpmMessageSendWhenTaskTimeoutReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("taskName", reqDTO.getTaskName());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        Long assigneeUserId = NumberUtil.parseLong(reqDTO.getAssigneeUserId(), null);
        if (assigneeUserId == null) {
            log.debug("[sendMessageWhenTaskTimeout][assigneeUserId({}) 非数字，跳过短信通知]", reqDTO.getAssigneeUserId());
            return;
        }
        smsSendApi.sendSingleSmsToAdmin(BpmMessageConvert.INSTANCE.convert(assigneeUserId,
                BpmMessageEnum.TASK_TIMEOUT.getSmsTemplateCode(), templateParams));
    }

    private String getProcessInstanceDetailUrl(String taskId) {
        return webProperties.getAdminUi().getUrl() + "/bpm/process-instance/detail?id=" + taskId;
    }

}
