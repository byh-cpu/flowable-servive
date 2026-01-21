package cn.iocoder.zhgd.module.bpm.service.task;

import java.util.List;

/**
 * 流程实例业务索引 Service
 */
public interface BpmProcessInstanceBizIndexService {

    /**
     * 创建流程实例业务索引
     *
     * @param processInstanceId 流程实例编号
     * @param companyId 企业编号
     * @param projectId 项目编号
     * @param startUserId 发起人编号
     */
    void createIndex(String processInstanceId, Long companyId, Long projectId, String startUserId);

    /**
     * 按 companyId / projectId 查询流程实例编号列表
     */
    List<String> listProcessInstanceIds(Long companyId, Long projectId);

}
