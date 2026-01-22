package cn.iocoder.zhgd.module.bpm.api.task.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程实例精简 Response DTO
 */
@Data
public class BpmProcessInstanceSimpleRespDTO implements Serializable {

    private String id;
    private String name;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer status;

    private String processDefinitionId;
    private String processDefinitionKey;
    private String category;

    private String startUserId;

}
