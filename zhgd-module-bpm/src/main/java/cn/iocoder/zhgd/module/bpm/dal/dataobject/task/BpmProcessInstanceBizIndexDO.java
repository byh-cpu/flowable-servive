package cn.iocoder.zhgd.module.bpm.dal.dataobject.task;

import cn.iocoder.zhgd.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 流程实例业务索引 DO
 *
 * 用于按 companyId / projectId 过滤流程与任务列表
 */
@TableName(value = "bpm_process_instance_biz_index", autoResultMap = true)
@KeySequence("bpm_process_instance_biz_index_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BpmProcessInstanceBizIndexDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 流程实例编号
     */
    private String processInstanceId;

    /**
     * 企业编号
     */
    private Long companyId;

    /**
     * 项目编号
     */
    private Long projectId;

    /**
     * 发起人编号
     */
    private String startUserId;

}
