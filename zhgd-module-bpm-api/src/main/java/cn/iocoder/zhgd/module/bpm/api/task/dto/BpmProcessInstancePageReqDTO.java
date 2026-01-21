package cn.iocoder.zhgd.module.bpm.api.task.dto;

import cn.iocoder.zhgd.framework.common.pojo.PageParam;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程实例分页 Request DTO
 */
@Data
public class BpmProcessInstancePageReqDTO extends PageParam {

    /**
     * 流程名称
     */
    private String name;

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
     * 流程实例的状态
     */
    private Integer status;

    /**
     * 流程分类
     */
    private String category;

    /**
     * 创建时间
     */
    private LocalDateTime[] createTime;

    /**
     * 结束时间
     */
    private LocalDateTime[] endTime;

    /**
     * 动态表单字段查询 JSON Str
     */
    private String formFieldsParams;

}
