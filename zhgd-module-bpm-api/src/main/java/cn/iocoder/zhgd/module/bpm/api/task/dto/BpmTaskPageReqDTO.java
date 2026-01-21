package cn.iocoder.zhgd.module.bpm.api.task.dto;

import cn.iocoder.zhgd.framework.common.pojo.PageParam;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程任务分页 Request DTO
 */
@Data
public class BpmTaskPageReqDTO extends PageParam {

    /**
     * 流程任务名
     */
    private String name;

    /**
     * 流程分类
     */
    private String category;

    /**
     * 流程定义的标识
     */
    private String processDefinitionKey;

    /**
     * 企业编号
     */
    private Long companyId;

    /**
     * 项目编号
     */
    private Long projectId;

    /**
     * 审批状态（已办时使用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime[] createTime;

}
