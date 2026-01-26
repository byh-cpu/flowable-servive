package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceCancelReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import jakarta.validation.Valid;

/**
 * 流程实例 Api 接口
 *
 * @author 芋道源码
 */
public interface BpmProcessInstanceApi {

    /**
     * 创建流程实例（提供给内部）
     *
     * @param userId 用户编号
     * @param reqDTO 创建信息
     * @return 实例的编号
     */
    String createProcessInstance(String userId, @Valid BpmProcessInstanceCreateReqDTO reqDTO);

    /**
     * 发起人取消流程实例
     *
     * @param userId 登录用户
     * @param reqDTO 取消信息
     */
    void cancelProcessInstanceByStartUser(String userId, @Valid BpmProcessInstanceCancelReqDTO reqDTO);

    /**
     * 管理员取消流程实例
     *
     * @param userId 登录用户
     * @param reqDTO 取消信息
     */
    void cancelProcessInstanceByAdmin(String userId, @Valid BpmProcessInstanceCancelReqDTO reqDTO);

}
