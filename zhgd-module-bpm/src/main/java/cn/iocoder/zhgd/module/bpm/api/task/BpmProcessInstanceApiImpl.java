package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.framework.common.util.object.BeanUtils;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceCancelReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.zhgd.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Flowable 流程实例 Api 实现类
 *
 * @author 芋道源码
 * @author jason
 */
@Service
@DubboService
@Validated
public class BpmProcessInstanceApiImpl implements BpmProcessInstanceApi {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public String createProcessInstance(String userId, @Valid BpmProcessInstanceCreateReqDTO reqDTO) {
        return processInstanceService.createProcessInstance(userId, reqDTO);
    }

    @Override
    public void cancelProcessInstanceByStartUser(String userId, @Valid BpmProcessInstanceCancelReqDTO reqDTO) {
        BpmProcessInstanceCancelReqVO reqVO = BeanUtils.toBean(reqDTO, BpmProcessInstanceCancelReqVO.class);
        processInstanceService.cancelProcessInstanceByStartUser(userId, reqVO);
    }

    @Override
    public void cancelProcessInstanceByAdmin(String userId, @Valid BpmProcessInstanceCancelReqDTO reqDTO) {
        BpmProcessInstanceCancelReqVO reqVO = BeanUtils.toBean(reqDTO, BpmProcessInstanceCancelReqVO.class);
        processInstanceService.cancelProcessInstanceByAdmin(userId, reqVO);
    }

}
