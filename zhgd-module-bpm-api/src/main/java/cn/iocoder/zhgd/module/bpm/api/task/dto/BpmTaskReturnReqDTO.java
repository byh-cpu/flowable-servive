package cn.iocoder.zhgd.module.bpm.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程任务退回 Request DTO
 */
@Data
public class BpmTaskReturnReqDTO implements Serializable {

    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @NotEmpty(message = "退回到的任务 Key 不能为空")
    private String targetTaskDefinitionKey;

    @NotEmpty(message = "退回意见不能为空")
    private String reason;

}
