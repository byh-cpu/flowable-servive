package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.framework.common.pojo.PageResult;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskPageReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskSimpleRespDTO;
import jakarta.validation.Valid;

/**
 * 流程任务查询 Api 接口（Dubbo）
 */
public interface BpmTaskQueryApi {

    /**
     * 待我审批的任务分页
     *
     * @param userId 登录用户
     * @param pageReqDTO 分页参数
     */
    PageResult<BpmTaskSimpleRespDTO> getTodoTaskPage(Long userId, @Valid BpmTaskPageReqDTO pageReqDTO);

    /**
     * 已审批完成的任务分页
     *
     * @param userId 登录用户
     * @param pageReqDTO 分页参数
     */
    PageResult<BpmTaskSimpleRespDTO> getDoneTaskPage(Long userId, @Valid BpmTaskPageReqDTO pageReqDTO);

    /**
     * 审批列表分页（全部任务）
     *
     * @param userId 登录用户
     * @param pageReqDTO 分页参数
     */
    PageResult<BpmTaskSimpleRespDTO> getManagerTaskPage(Long userId, @Valid BpmTaskPageReqDTO pageReqDTO);

}
