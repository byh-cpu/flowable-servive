package cn.iocoder.zhgd.module.bpm.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 流程任务加签 Request DTO
 */
@Data
public class BpmTaskSignCreateReqDTO implements Serializable {

    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @NotEmpty(message = "加签用户不能为空")
    private Set<String> userIds;

    @NotEmpty(message = "加签类型不能为空")
    private String type;

    @NotEmpty(message = "加签原因不能为空")
    private String reason;

}
