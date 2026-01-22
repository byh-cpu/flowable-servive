package cn.iocoder.zhgd.module.bpm.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 流程任务审批通过 Request DTO
 */
@Data
public class BpmTaskApproveReqDTO implements Serializable {

    @NotEmpty(message = "任务编号不能为空")
    private String id;

    private String reason;

    private String signPicUrl;

    /**
     * 变量实例（动态表单）
     */
    private Map<String, Object> variables;

    /**
     * 下一个节点审批人
     */
    private Map<String, List<String>> nextAssignees;

}
