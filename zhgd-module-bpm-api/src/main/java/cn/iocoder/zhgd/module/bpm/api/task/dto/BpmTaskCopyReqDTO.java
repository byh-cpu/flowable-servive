package cn.iocoder.zhgd.module.bpm.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

/**
 * 流程任务抄送 Request DTO
 */
@Data
public class BpmTaskCopyReqDTO implements Serializable {

    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @NotEmpty(message = "抄送用户不能为空")
    private Collection<String> copyUserIds;

    private String reason;

}
