package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.framework.common.util.object.BeanUtils;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskApproveReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskRejectReqDTO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.zhgd.module.bpm.service.task.BpmTaskService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 流程任务操作 Api 实现类
 */
@Service
@DubboService
@Validated
public class BpmTaskOperateApiImpl implements BpmTaskOperateApi {

    @Resource
    private BpmTaskService taskService;

    @Override
    public void approveTask(String userId, @Valid BpmTaskApproveReqDTO reqDTO) {
        BpmTaskApproveReqVO reqVO = BeanUtils.toBean(reqDTO, BpmTaskApproveReqVO.class);
        taskService.approveTask(userId, reqVO);
    }

    @Override
    public void rejectTask(String userId, @Valid BpmTaskRejectReqDTO reqDTO) {
        BpmTaskRejectReqVO reqVO = BeanUtils.toBean(reqDTO, BpmTaskRejectReqVO.class);
        taskService.rejectTask(userId, reqVO);
    }

}
