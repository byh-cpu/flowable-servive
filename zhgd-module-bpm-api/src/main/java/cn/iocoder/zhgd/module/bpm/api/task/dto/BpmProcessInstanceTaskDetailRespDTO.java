package cn.iocoder.zhgd.module.bpm.api.task.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 流程实例标准详情（含任务信息） Response DTO
 */
@Data
public class BpmProcessInstanceTaskDetailRespDTO implements Serializable {

    /**
     * 流程实例信息
     */
    private BpmProcessInstanceLiteRespDTO processInstance;

    /**
     * 流程实例变量（全局表单数据）
     */
    private Map<String, Object> formVariables;

    /**
     * 任务列表
     */
    private List<TaskDetail> tasks;

    @Data
    public static class TaskDetail implements Serializable {

        private String id;
        private String name;
        private String taskDefinitionKey;
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime endTime;

        private String assigneeUserId;
        private String ownerUserId;

        /**
         * 任务变量（局部表单数据）
         */
        private Map<String, Object> taskVariables;

        /**
         * 候选人策略（见 BpmTaskCandidateStrategyEnum）
         */
        private Integer candidateStrategy;

        /**
         * 用户维度候选人
         */
        private List<String> candidateUserIds;

        /**
         * 角色维度候选人
         */
        private List<String> candidateRoleIds;

    }

}
