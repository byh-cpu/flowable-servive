package cn.iocoder.zhgd.module.bpm.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程任务委派 Request DTO
 */
@Data
public class BpmTaskDelegateReqDTO implements Serializable {

    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @NotEmpty(message = "被委派人 ID 不能为空")
    private String delegateUserId;

    @NotEmpty(message = "委派原因不能为空")
    private String reason;

}
