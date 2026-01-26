package cn.iocoder.zhgd.module.bpm.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程实例取消 Request DTO
 */
@Data
public class BpmProcessInstanceCancelReqDTO implements Serializable {

    @NotEmpty(message = "流程实例的编号不能为空")
    private String id;

    @NotEmpty(message = "取消原因不能为空")
    private String reason;

}
