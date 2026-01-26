package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.framework.common.util.object.BeanUtils;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskApproveReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskCopyReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskDelegateReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskRejectReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskReturnReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskSignCreateReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskSignDeleteReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmTaskTransferReqDTO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskCopyReqVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskDelegateReqVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskReturnReqVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskSignCreateReqVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskSignDeleteReqVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.BpmTaskTransferReqVO;
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

    @Override
    public void transferTask(String userId, @Valid BpmTaskTransferReqDTO reqDTO) {
        BpmTaskTransferReqVO reqVO = BeanUtils.toBean(reqDTO, BpmTaskTransferReqVO.class);
        taskService.transferTask(userId, reqVO);
    }

    @Override
    public void returnTask(String userId, @Valid BpmTaskReturnReqDTO reqDTO) {
        BpmTaskReturnReqVO reqVO = BeanUtils.toBean(reqDTO, BpmTaskReturnReqVO.class);
        taskService.returnTask(userId, reqVO);
    }

    @Override
    public void delegateTask(String userId, @Valid BpmTaskDelegateReqDTO reqDTO) {
        BpmTaskDelegateReqVO reqVO = BeanUtils.toBean(reqDTO, BpmTaskDelegateReqVO.class);
        taskService.delegateTask(userId, reqVO);
    }

    @Override
    public void createSignTask(String userId, @Valid BpmTaskSignCreateReqDTO reqDTO) {
        BpmTaskSignCreateReqVO reqVO = BeanUtils.toBean(reqDTO, BpmTaskSignCreateReqVO.class);
        taskService.createSignTask(userId, reqVO);
    }

    @Override
    public void deleteSignTask(String userId, @Valid BpmTaskSignDeleteReqDTO reqDTO) {
        BpmTaskSignDeleteReqVO reqVO = BeanUtils.toBean(reqDTO, BpmTaskSignDeleteReqVO.class);
        taskService.deleteSignTask(userId, reqVO);
    }

    @Override
    public void copyTask(String userId, @Valid BpmTaskCopyReqDTO reqDTO) {
        BpmTaskCopyReqVO reqVO = BeanUtils.toBean(reqDTO, BpmTaskCopyReqVO.class);
        taskService.copyTask(userId, reqVO);
    }

    @Override
    public void withdrawTask(String userId, String taskId) {
        taskService.withdrawTask(userId, taskId);
    }

}
