package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.framework.common.pojo.PageResult;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstancePageReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceSimpleRespDTO;
import jakarta.validation.Valid;

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

}
