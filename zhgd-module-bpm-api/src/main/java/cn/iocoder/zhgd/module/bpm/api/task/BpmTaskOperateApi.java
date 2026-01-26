package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskApproveReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskCopyReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskDelegateReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskRejectReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskReturnReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskSignCreateReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskSignDeleteReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskTransferReqDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

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

    /**
     * 转办任务
     *
     * @param userId 登录用户
     * @param reqDTO 转办请求
     */
    void transferTask(String userId, @Valid BpmTaskTransferReqDTO reqDTO);

    /**
     * 退回任务
     *
     * @param userId 登录用户
     * @param reqDTO 退回请求
     */
    void returnTask(String userId, @Valid BpmTaskReturnReqDTO reqDTO);

    /**
     * 委派任务
     *
     * @param userId 登录用户
     * @param reqDTO 委派请求
     */
    void delegateTask(String userId, @Valid BpmTaskDelegateReqDTO reqDTO);

    /**
     * 加签任务
     *
     * @param userId 登录用户
     * @param reqDTO 加签请求
     */
    void createSignTask(String userId, @Valid BpmTaskSignCreateReqDTO reqDTO);

    /**
     * 减签任务
     *
     * @param userId 登录用户
     * @param reqDTO 减签请求
     */
    void deleteSignTask(String userId, @Valid BpmTaskSignDeleteReqDTO reqDTO);

    /**
     * 抄送任务
     *
     * @param userId 登录用户
     * @param reqDTO 抄送请求
     */
    void copyTask(String userId, @Valid BpmTaskCopyReqDTO reqDTO);

    /**
     * 撤回任务
     *
     * @param userId 登录用户
     * @param taskId 任务编号
     */
    void withdrawTask(String userId, @NotEmpty(message = "任务编号不能为空") String taskId);

}
