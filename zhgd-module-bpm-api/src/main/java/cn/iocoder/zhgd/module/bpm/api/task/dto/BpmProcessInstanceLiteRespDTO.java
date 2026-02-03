package cn.iocoder.zhgd.module.bpm.api.task.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程实例轻量详情 Response DTO
 */
@Data
public class BpmProcessInstanceLiteRespDTO implements Serializable {

    private String id;
    private String name;
    /**
     * 流程标题（拼接后的标题）
     */
    private String title;
    private String businessKey;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer status;

    private String processDefinitionId;
    private String processDefinitionKey;
    private String category;

    private String startUserId;

    /**
     * 流程实例变量（全局表单数据）
     */
    private Map<String, Object> formVariables;

}
