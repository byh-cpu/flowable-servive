package cn.iocoder.zhgd.module.bpm.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程任务审批不通过 Request DTO
 */
@Data
public class BpmTaskRejectReqDTO implements Serializable {

    @NotEmpty(message = "任务编号不能为空")
    private String id;

    private String reason;

}
