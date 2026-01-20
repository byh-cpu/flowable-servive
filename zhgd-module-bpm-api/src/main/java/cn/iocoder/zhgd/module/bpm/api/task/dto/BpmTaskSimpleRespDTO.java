package cn.iocoder.zhgd.module.bpm.api.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程任务精简 Response DTO
 */
@Data
public class BpmTaskSimpleRespDTO {

    private String id;
    private String name;
    private String taskDefinitionKey;
    private Integer status;
    private String reason;

    private LocalDateTime createTime;
    private LocalDateTime endTime;

    private String processInstanceId;
    private String processInstanceName;

    private String processDefinitionId;
    private String processDefinitionKey;
    private String category;

    private Long startUserId;
    private Long assigneeUserId;
    private Long ownerUserId;

}
