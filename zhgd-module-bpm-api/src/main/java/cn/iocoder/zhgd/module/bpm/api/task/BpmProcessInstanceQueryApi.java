package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.framework.common.pojo.PageResult;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceLiteRespDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstancePageReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceSimpleRespDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceTaskDetailRespDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * 流程实例查询 Api 接口（Dubbo）
 */
public interface BpmProcessInstanceQueryApi {

    /**
     * 我申请的流程分页
     *
     * @param userId 登录用户
     * @param pageReqDTO 分页参数
     */
    PageResult<BpmProcessInstanceSimpleRespDTO> getMyProcessInstancePage(String userId,
                                                                        @Valid BpmProcessInstancePageReqDTO pageReqDTO);

    /**
     * 获得流程实例轻量详情
     *
     * @param processInstanceId 流程实例编号
     * @return 流程实例轻量详情
     */
    BpmProcessInstanceLiteRespDTO getProcessInstanceDetailLite(
            @NotEmpty(message = "流程实例编号不能为空") String processInstanceId);

    /**
     * 获得流程实例标准详情（含任务信息）
     *
     * @param processInstanceId 流程实例编号
     * @return 流程实例标准详情
     */
    BpmProcessInstanceTaskDetailRespDTO getProcessInstanceDetail(
            @NotEmpty(message = "流程实例编号不能为空") String processInstanceId);

}
