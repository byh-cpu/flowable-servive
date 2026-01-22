package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskApproveReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskRejectReqDTO;
import jakarta.validation.Valid;

/**
 * 流程任务操作 Api 接口（Dubbo）
 */
public interface BpmTaskOperateApi {

    /**
     * 审批通过
     *
     * @param userId 登录用户
     * @param reqDTO 审批请求
     */
    void approveTask(String userId, @Valid BpmTaskApproveReqDTO reqDTO);

    /**
     * 审批不通过
     *
     * @param userId 登录用户
     * @param reqDTO 审批请求
     */
    void rejectTask(String userId, @Valid BpmTaskRejectReqDTO reqDTO);

}
