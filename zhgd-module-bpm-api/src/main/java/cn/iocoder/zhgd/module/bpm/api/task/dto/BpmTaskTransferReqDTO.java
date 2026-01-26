package cn.iocoder.zhgd.module.bpm.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程任务转办 Request DTO
 */
@Data
public class BpmTaskTransferReqDTO implements Serializable {

    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @NotEmpty(message = "新审批人的用户编号不能为空")
    private String assigneeUserId;

    @NotEmpty(message = "转办原因不能为空")
    private String reason;

}
